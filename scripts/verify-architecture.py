from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
BASE_PACKAGE = "com.ssajudn.bareuang"

FORBIDDEN_REFERENCES = {
    "domain": [
        re.compile(r"(?<![A-Za-z0-9_.])android(?:x)?\."),
        re.compile(rf"(?<![A-Za-z0-9_.]){re.escape(BASE_PACKAGE)}\.(?!domain(?:\.|$|\s))"),
    ],
    "data": [
        re.compile(rf"(?<![A-Za-z0-9_.]){re.escape(BASE_PACKAGE)}\.(?:presentation|ui)\."),
        re.compile(rf"(?<![A-Za-z0-9_.]){re.escape(BASE_PACKAGE)}\.(?:MainActivity|BareuangApplication|widget)\b"),
    ],
    "presentation": [
        re.compile(rf"(?<![A-Za-z0-9_.]){re.escape(BASE_PACKAGE)}\.data\."),
        re.compile(rf"(?<![A-Za-z0-9_.]){re.escape(BASE_PACKAGE)}\.(?:MainActivity|BareuangApplication|widget)\b"),
    ],
}

ALLOWED_PROJECT_DEPENDENCIES = {
    "domain": set(),
    "data": {"domain"},
    "presentation": {"domain"},
    "app": {"domain", "data", "presentation"},
}

TEST_PACKAGE_PREFIXES = {
    "domain": (f"{BASE_PACKAGE}.domain",),
    "data": (f"{BASE_PACKAGE}.data",),
    "presentation": (
        f"{BASE_PACKAGE}.ui",
        f"{BASE_PACKAGE}.utils",
        f"{BASE_PACKAGE}.testutil",
    ),
    # App tests are reserved for composition-root and application integration.
    "app": (BASE_PACKAGE,),
}


def main() -> int:
    violations = []
    settings_file = ROOT / "settings.gradle.kts"
    settings = settings_file.read_text() if settings_file.exists() else ""
    for module in ALLOWED_PROJECT_DEPENDENCIES:
        if not re.search(rf'include\(\s*"\s*:{re.escape(module)}\s*"\s*\)', settings):
            violations.append(f"settings.gradle.kts: module :{module} is missing")

    for module, patterns in FORBIDDEN_REFERENCES.items():
        source_root = ROOT / module / "src"
        for source in source_root.rglob("*.kt"):
            for line_number, line in enumerate(source.read_text().splitlines(), start=1):
                if any(pattern.search(line) for pattern in patterns):
                    violations.append(
                        f"{source.relative_to(ROOT)}:{line_number}: {line.strip()}"
                    )

    project_dependency_pattern = re.compile(
        r'project\(\s*"\s*:(?P<module>[A-Za-z0-9_-]+)\s*"\s*\)'
    )
    for module, allowed in ALLOWED_PROJECT_DEPENDENCIES.items():
        build_file = ROOT / module / "build.gradle.kts"
        if not build_file.exists():
            violations.append(f"{build_file.relative_to(ROOT)}: module build file is missing")
            continue
        actual = set(project_dependency_pattern.findall(build_file.read_text()))
        for dependency in sorted(actual - allowed):
            violations.append(
                f"{build_file.relative_to(ROOT)}: project dependency :{dependency} is not allowed"
            )
        for dependency in sorted(allowed - actual):
            violations.append(
                f"{build_file.relative_to(ROOT)}: expected project dependency :{dependency} is missing"
            )

    for module in ALLOWED_PROJECT_DEPENDENCIES:
        source_root = ROOT / module / "src"
        for source in source_root.rglob("*.kt"):
            parts = source.parts
            language_index = next(
                (index for index, part in enumerate(parts) if part in {"java", "kotlin"}),
                None,
            )
            if language_index is None or language_index < 2 or parts[language_index - 2] != "src":
                violations.append(f"{source.relative_to(ROOT)}: source is outside a standard src/<set>/{'{java,kotlin}'} root")
                continue

            package_match = re.search(r"^\s*package\s+([A-Za-z0-9_.]+)", source.read_text(), re.MULTILINE)
            if not package_match:
                violations.append(f"{source.relative_to(ROOT)}: missing package declaration")
                continue
            package = package_match.group(1)
            relative_directory = source.relative_to(Path(*parts[: language_index + 1]))
            expected_package = ".".join(relative_directory.parent.parts)
            if package != expected_package:
                violations.append(
                    f"{source.relative_to(ROOT)}: package {package} does not match directory {expected_package}"
                )

            source_set = parts[language_index - 1]
            if source_set in {"test", "androidTest"}:
                prefixes = TEST_PACKAGE_PREFIXES[module]
                if not any(package == prefix or package.startswith(prefix + ".") for prefix in prefixes):
                    violations.append(
                        f"{source.relative_to(ROOT)}: test package {package} is outside module {module}'s ownership"
                    )
                if module == "app" and package.startswith(
                    tuple(f"{BASE_PACKAGE}.{layer}" for layer in ("data", "domain", "presentation", "ui", "utils"))
                ):
                    violations.append(
                        f"{source.relative_to(ROOT)}: app integration tests must not own another module's package"
                    )

    if violations:
        print("Architecture boundary violations found:", file=sys.stderr)
        print("\n".join(violations), file=sys.stderr)
        return 1

    print(
        "Architecture verified: project dependencies, imports, Kotlin package paths, "
        "and test ownership follow the documented module boundaries."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
