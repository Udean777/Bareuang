package com.ssajudn.bareuang.ui.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.utils.DateUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: OcrScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(eff.message)
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(eff.uiText.asString(context))
                else -> {}
            }
        }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.processImage(it) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.processImage(it) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Struk") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.rawText != null) {
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Button(
                        onClick = { viewModel.save { onNavigateBack() } },
                        enabled = !uiState.isSaving && uiState.parsedAmount > 0 && uiState.selectedWalletId != null,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Simpan Transaksi • ${CurrencyFormatter.formatRupiah(uiState.parsedAmount)}")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallet selector
            var expanded by remember { mutableStateOf(false) }
            val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedWallet?.name ?: "Pilih dompet",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Dompet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.wallets.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { viewModel.onWalletSelected(w.id!!); expanded = false })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = ::launchCamera, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Kamera")
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Galeri")
                }
            }

            if (uiState.isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Memproses OCR...", style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.rawText == null && !uiState.isProcessing) {
                // Empty state with mini receipt illustration
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Mini blank receipt preview
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(140.dp)
                                .shadow(4.dp, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFFEF8))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0)))
                                Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFEEEEEE)))
                                DashedDivider()
                                repeat(3) { Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF5F5F5))) }
                                DashedDivider()
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFFFECB3)))
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Tips scan struk", style = MaterialTheme.typography.titleSmall)
                                Text("• Foto struk dengan cahaya cukup, teks tidak blur", style = MaterialTheme.typography.bodySmall)
                                Text("• Pastikan baris TOTAL / JUMLAH terlihat", style = MaterialTheme.typography.bodySmall)
                                Text("• Hasil OCR bisa diedit sebelum disimpan", style = MaterialTheme.typography.bodySmall)
                                Text("• 100% offline — ML Kit on-device", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (uiState.rawText != null) {
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = viewModel::onMerchantChange,
                    label = { Text("Merchant") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                AmountTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = "Total (Rp)",
                    modifier = Modifier.fillMaxWidth()
                )
                // Category
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(
                        value = uiState.category.displayName,
                        onValueChange = {}, readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        TransactionCategory.entries.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { viewModel.onCategoryChange(cat); catExpanded = false })
                        }
                    }
                }
                // Date
                var showDatePicker by remember { mutableStateOf(false) }
                if (showDatePicker) {
                    AppDatePickerDialog(
                        initialDateMillis = DateUtils.parseIsoToMillis(uiState.date),
                        onDateSelected = { viewModel.onDateChange(DateUtils.formatMillisToIso(it)) },
                        onDismiss = { showDatePicker = false }
                    )
                }
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(uiState.date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) { Text("Ubah") }
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.weight(1f)) { Text("Scan ulang") }
                    var showEditDialog by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { showEditDialog = true }, modifier = Modifier.weight(1f)) { Text("Perbaiki teks") }
                    if (showEditDialog) {
                        var edited by remember { mutableStateOf(uiState.rawText!!) }
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            title = { Text("Perbaiki susunan teks") },
                            text = {
                                OutlinedTextField(
                                    value = edited,
                                    onValueChange = { edited = it },
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.onRawTextEdited(edited)
                                    showEditDialog = false
                                }) { Text("Simpan") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditDialog = false }) { Text("Batal") }
                            }
                        )
                    }
                }

                ReceiptPaperCard(
                    rawText = uiState.rawText!!,
                    merchant = uiState.merchant,
                    total = uiState.parsedAmount
                )
            }
        }
    }
}

@Composable
private fun ReceiptPaperCard(rawText: String, merchant: String, total: Long) {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    // Heuristic: header = first 3 lines, footer = last 3 lines, body = middle
    val headerLines = lines.take(4)
    val footerLines = if (lines.size > 7) lines.takeLast(3) else emptyList()
    val bodyLines = when {
        lines.size > 7 -> lines.drop(4).dropLast(3)
        lines.size > 4 -> lines.drop(4)
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFEF8))
            .padding(0.dp)
    ) {
        // Top perforation
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val r = 6.dp.toPx()
            var x = r
            while (x < size.width) {
                drawCircle(Color(0xFFE0E0E0), radius = r, center = Offset(x, r / 2))
                x += r * 2
            }
        }
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Header centered bold
            headerLines.forEachIndexed { idx, line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = if (idx == 0) 16.sp else 11.sp,
                    fontWeight = if (idx == 0) FontWeight.ExtraBold else FontWeight.Normal,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 14.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            DashedDivider()
            // Body mono
            bodyLines.forEach { line ->
                val isTotal = line.uppercase().contains("TOTAL") || line.uppercase().contains("JUMLAH") || line.uppercase().contains("TAGIHAN")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Split price to right if contains numbers
                    val hasPrice = Regex("""\d[\d.,]*""").containsMatchIn(line)
                    if (hasPrice && line.length > 24) {
                        // try to align price right: split at last number
                        val m = Regex("""(.+?)(\s+Rp?\.?\s*\d[\d.,]*)${'$'}""").find(line)
                        if (m != null) {
                            Text(m.groupValues[1].trim(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B), fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            Text(m.groupValues[2].trim(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B))
                        } else {
                            Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF2B2B2B))
                        }
                    } else {
                        Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B), fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            if (bodyLines.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                DashedDivider()
            }
            // Total highlight
            if (total > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL", fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D4037))
                    Text(CurrencyFormatter.formatRupiah(total), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D4037))
                }
                Spacer(Modifier.height(6.dp))
                DashedDivider()
            }
            // Footer centered
            footerLines.forEach { line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF6D6D6D)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "— ${merchant} • struk asli —",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        // Bottom zigzag
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val zig = 10.dp.toPx()
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                var x = 0f
                var up = true
                while (x < size.width) {
                    lineTo(x + zig / 2, if (up) zig else 0f)
                    x += zig / 2
                    up = !up
                }
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color(0xFFFFFEF8))
        }
        // Caption outside paper
        Text(
            text = "Preview struk • hasil OCR dapat diedit di atas",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DashedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 4.dp)) {
        val dash = 6.dp.toPx()
        val gap = 4.dp.toPx()
        drawLine(
            color = Color(0xFFBDBDBD),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
        )
    }
}
