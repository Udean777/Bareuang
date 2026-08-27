#!/usr/bin/env python3
"""
Bareuang Open Graph (OG) Banner Generator
Generates 1200x630 PNG and WebP assets for WhatsApp / Twitter / FB link previews.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1200, 630

# Resolve paths
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../..'))
font_dir = os.path.join(BASE_DIR, 'presentation/src/main/res/font')
art_logo = os.path.join(BASE_DIR, 'art/app_logo_new.png')
dash_mockup = os.path.join(BASE_DIR, 'web/assets/screenshots/Dashboard.png')
out_png = os.path.join(BASE_DIR, 'web/assets/og-image.png')
out_webp = os.path.join(BASE_DIR, 'web/assets/og-image.webp')

def generate():
    # Load typography
    f_brand = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_extrabold.ttf'), 34)
    f_badge = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_extrabold.ttf'), 13)
    f_title = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_extrabold.ttf'), 50)
    f_sub = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_medium.ttf'), 20)
    f_pill = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_bold.ttf'), 16)
    f_pill_sub = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_regular.ttf'), 13)
    f_url = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_extrabold.ttf'), 18)
    f_url_sub = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_medium.ttf'), 15)

    # 1. Base Gradient Canvas
    c_tl = (255, 253, 246)
    c_br = (255, 235, 192)
    canvas = Image.new('RGBA', (W, H))
    for y in range(H):
        for x in range(W):
            t = (x * 0.4 + y * 0.6) / (W * 0.4 + H * 0.6)
            r = int(c_tl[0] + (c_br[0] - c_tl[0]) * t)
            g = int(c_tl[1] + (c_br[1] - c_tl[1]) * t)
            b = int(c_tl[2] + (c_br[2] - c_tl[2]) * t)
            canvas.putpixel((x, y), (r, g, b, 255))

    # 2. Ambient background glows
    ambient = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d_amb = ImageDraw.Draw(ambient)
    d_amb.ellipse([780, -120, 1320, 420], fill=(244, 162, 22, 45))
    d_amb.ellipse([600, 300, 1220, 750], fill=(255, 184, 46, 55))
    d_amb.ellipse([-100, 350, 450, 800], fill=(244, 162, 22, 25))
    ambient = ambient.filter(ImageFilter.GaussianBlur(75))
    canvas = Image.alpha_composite(canvas, ambient)

    # Decorative background curves/rings
    decor = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d_decor = ImageDraw.Draw(decor)
    d_decor.ellipse([820, -100, 1380, 460], outline=(240, 222, 176, 85), width=2)
    d_decor.ellipse([760, -160, 1440, 520], outline=(240, 222, 176, 45), width=2)
    canvas = Image.alpha_composite(canvas, decor)

    # 3. Layer for UI
    ui_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(ui_layer)

    # Left: Brand
    logo_img = Image.open(art_logo).convert('RGBA')
    logo_thumb = logo_img.resize((54, 54), Image.Resampling.LANCZOS)

    lx, ly = 75, 48
    logo_card = Image.new('RGBA', (60, 60), (255, 255, 255, 255))
    mask_l = Image.new('L', (60, 60), 0)
    ImageDraw.Draw(mask_l).rounded_rectangle([0, 0, 59, 59], radius=18, fill=255)
    logo_card.paste(logo_thumb, (3, 3), logo_thumb)

    l_sh = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(l_sh).rounded_rectangle([lx, ly + 2, lx + 60, ly + 62], radius=18, fill=(62, 42, 0, 30))
    l_sh = l_sh.filter(ImageFilter.GaussianBlur(8))
    canvas = Image.alpha_composite(canvas, l_sh)
    canvas.paste(logo_card, (lx, ly), mask_l)

    d.text((150, 50), 'Bareuang', font=f_brand, fill=(62, 42, 0, 255))

    badge_x, badge_y = 320, 54
    d.rounded_rectangle([badge_x, badge_y, badge_x + 138, badge_y + 30], radius=15, fill=(255, 241, 204, 255), outline=(240, 222, 176, 255), width=1)
    d.ellipse([badge_x + 12, badge_y + 11, badge_x + 18, badge_y + 17], fill=(34, 139, 34, 255))
    d.text((badge_x + 26, badge_y + 7), '100% OFFLINE', font=f_badge, fill=(107, 74, 18, 255))

    # Headline
    title_y = 135
    d.text((75, title_y), 'Tahu sampai kapan', font=f_title, fill=(26, 18, 0, 255))
    d.text((75, title_y + 60), 'uangmu tahan.', font=f_title, fill=(217, 119, 6, 255))

    # Subtitle
    sub_y = 272
    d.text((75, sub_y), 'Teman cozy buat kelola keuangan harian.', font=f_sub, fill=(107, 74, 18, 255))
    d.text((75, sub_y + 28), 'Tanpa akun, tanpa internet, 100% privat di HP.', font=f_sub, fill=(122, 106, 74, 255))

    # Features
    features = [
        ('runway', 'Financial Runway Tracker', 'Prediksi sisa hari bertahan & target aman'),
        ('wallet', 'Multi-Wallet & Budget', 'Atur pos rekening, e-wallet, dan cash'),
        ('ocr', 'Scan Struk OCR On-Device', 'Catat pengeluaran instan tanpa ngetik')
    ]

    pill_y = 356
    p_sh = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d_psh = ImageDraw.Draw(p_sh)
    for f_type, title_f, desc_f in features:
        d_psh.rounded_rectangle([75, pill_y + 3, 530, pill_y + 55], radius=18, fill=(62, 42, 0, 18))
        pill_y += 66
    p_sh = p_sh.filter(ImageFilter.GaussianBlur(8))
    canvas = Image.alpha_composite(canvas, p_sh)

    pill_y = 356
    for f_type, title_f, desc_f in features:
        d.rounded_rectangle([75, pill_y, 530, pill_y + 54], radius=18, fill=(255, 255, 255, 235), outline=(240, 222, 176, 255), width=1)
        ic_cx, ic_cy = 104, pill_y + 27
        d.rounded_rectangle([85, pill_y + 8, 123, pill_y + 46], radius=12, fill=(255, 245, 218, 255))
        
        if f_type == 'runway':
            d.ellipse([ic_cx - 10, ic_cy - 10, ic_cx + 10, ic_cy + 10], outline=(217, 119, 6, 255), width=2)
            d.line([ic_cx, ic_cy - 6, ic_cx, ic_cy, ic_cx + 5, ic_cy], fill=(217, 119, 6, 255), width=2)
        elif f_type == 'wallet':
            d.rounded_rectangle([ic_cx - 11, ic_cy - 8, ic_cx + 11, ic_cy + 8], radius=3, outline=(217, 119, 6, 255), width=2)
            d.line([ic_cx - 11, ic_cy - 2, ic_cx + 11, ic_cy - 2], fill=(217, 119, 6, 255), width=2)
            d.ellipse([ic_cx + 2, ic_cy + 1, ic_cx + 6, ic_cy + 5], fill=(217, 119, 6, 255))
        elif f_type == 'ocr':
            d.rounded_rectangle([ic_cx - 9, ic_cy - 10, ic_cx + 9, ic_cy + 10], radius=2, outline=(217, 119, 6, 255), width=2)
            d.line([ic_cx - 5, ic_cy - 5, ic_cx + 5, ic_cy - 5], fill=(217, 119, 6, 255), width=2)
            d.line([ic_cx - 5, ic_cy, ic_cx + 5, ic_cy], fill=(217, 119, 6, 255), width=2)
            d.line([ic_cx - 5, ic_cy + 5, ic_cx + 1, ic_cy + 5], fill=(217, 119, 6, 255), width=2)

        d.text((135, pill_y + 8), title_f, font=f_pill, fill=(62, 42, 0, 255))
        d.text((135, pill_y + 30), desc_f, font=f_pill_sub, fill=(138, 118, 86, 255))
        pill_y += 66

    url_y = 572
    d.text((75, url_y), 'bareuang.vercel.app', font=f_url, fill=(107, 74, 18, 255))
    d.text((275, url_y + 1), '·  Aplikasi Android Gratis & Open Privacy', font=f_url_sub, fill=(145, 126, 92, 255))

    # Right: Phone Mockup & Mascot
    dash_raw = Image.open(dash_mockup).convert('RGBA')
    phone_w, phone_h = 280, 565
    phone_x, phone_y = 845, 35

    p_shadow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(p_shadow).rounded_rectangle([phone_x - 12, phone_y + 12, phone_x + phone_w + 12, phone_y + phone_h + 20], radius=44, fill=(62, 42, 0, 60))
    p_shadow = p_shadow.filter(ImageFilter.GaussianBlur(30))
    canvas = Image.alpha_composite(canvas, p_shadow)

    phone_frame = Image.new('RGBA', (phone_w, phone_h), (0, 0, 0, 0))
    p_frame_draw = ImageDraw.Draw(phone_frame)
    p_frame_draw.rounded_rectangle([0, 0, phone_w - 1, phone_h - 1], radius=38, fill=(28, 22, 16, 255), outline=(95, 80, 65, 255), width=3)

    screen_w, screen_h = phone_w - 16, phone_h - 16
    dash_resized = dash_raw.resize((screen_w, screen_h), Image.Resampling.LANCZOS)
    screen_mask = Image.new('L', (screen_w, screen_h), 0)
    ImageDraw.Draw(screen_mask).rounded_rectangle([0, 0, screen_w - 1, screen_h - 1], radius=30, fill=255)
    phone_frame.paste(dash_resized, (8, 8), screen_mask)
    ImageDraw.Draw(phone_frame).rounded_rectangle([phone_w // 2 - 28, 14, phone_w // 2 + 28, 28], radius=7, fill=(15, 12, 8, 255))
    canvas.paste(phone_frame, (phone_x, phone_y), phone_frame)

    # Mascot
    bear_img = Image.open(art_logo).convert('RGBA')
    bear_size = 215
    bear_resized = bear_img.resize((bear_size, bear_size), Image.Resampling.LANCZOS)
    bear_x, bear_y = 660, 345

    bear_sh = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(bear_sh).ellipse([bear_x + 25, bear_y + bear_size - 25, bear_x + bear_size - 25, bear_y + bear_size + 15], fill=(62, 42, 0, 50))
    bear_sh = bear_sh.filter(ImageFilter.GaussianBlur(16))
    canvas = Image.alpha_composite(canvas, bear_sh)
    canvas.paste(bear_resized, (bear_x, bear_y), bear_resized)

    # Status card
    card_w, card_h = 215, 80
    card_x, card_y = 645, 150
    card_sh = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(card_sh).rounded_rectangle([card_x, card_y + 4, card_x + card_w, card_y + card_h + 4], radius=18, fill=(62, 42, 0, 35))
    card_sh = card_sh.filter(ImageFilter.GaussianBlur(14))
    canvas = Image.alpha_composite(canvas, card_sh)

    card = Image.new('RGBA', (card_w, card_h), (0, 0, 0, 0))
    c_draw = ImageDraw.Draw(card)
    c_draw.rounded_rectangle([0, 0, card_w - 1, card_h - 1], radius=18, fill=(255, 255, 255, 250), outline=(240, 222, 176, 255), width=1)
    f_c_label = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_bold.ttf'), 11)
    f_c_val = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_extrabold.ttf'), 22)
    f_c_status = ImageFont.truetype(os.path.join(font_dir, 'plus_jakarta_sans_bold.ttf'), 12)

    c_draw.text((16, 12), 'FINANCIAL RUNWAY', font=f_c_label, fill=(138, 118, 86, 255))
    c_draw.text((16, 32), '4.2 Bulan', font=f_c_val, fill=(62, 42, 0, 255))
    c_draw.rounded_rectangle([card_w - 74, 34, card_w - 14, 58], radius=8, fill=(235, 248, 238, 255), outline=(180, 230, 190, 255), width=1)
    c_draw.text((card_w - 64, 39), '● Aman', font=f_c_status, fill=(34, 139, 34, 255))
    canvas.paste(card, (card_x, card_y), card)

    canvas = Image.alpha_composite(canvas, ui_layer)
    canvas_rgb = canvas.convert('RGB')
    canvas_rgb.save(out_png, 'PNG', optimize=True)
    canvas_rgb.save(out_webp, 'WEBP', quality=95, method=6)
    print(f'Generated {out_png} ({os.path.getsize(out_png)} bytes)')
    print(f'Generated {out_webp} ({os.path.getsize(out_webp)} bytes)')

if __name__ == '__main__':
    generate()
