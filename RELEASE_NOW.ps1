# Touchgrass release script — run from PowerShell in D:\My_Projects\Touchgrass
#
#   powershell -ExecutionPolicy Bypass -File .\RELEASE_NOW.ps1
#
# Step 1 commits and pushes main. That is always safe.
# Step 2 pushes the release tag, which triggers the signed build in GitHub Actions.
# It will NOT tag until you confirm the four signing secrets exist, because a tag
# pushed without them fails the build and burns that version number.
#
# Read STATUS.md before running this.

$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

# Read the version out of the build file so this script can never disagree with it.
$gradle  = Get-Content .\app\build.gradle.kts -Raw
$version = [regex]::Match($gradle, 'versionName\s*=\s*"([^"]+)"').Groups[1].Value
if (-not $version) { throw "Could not read versionName from app/build.gradle.kts" }
$tag = "v$version"
Write-Host "Version from build.gradle.kts: $version  ->  tag $tag" -ForegroundColor Cyan

Write-Host "`n== 1. clearing stale git locks ==" -ForegroundColor Cyan
Remove-Item -Force -ErrorAction SilentlyContinue .git\index.lock
Get-ChildItem -Path .git\objects -Recurse -Filter "tmp_obj_*" -ErrorAction SilentlyContinue |
    Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "`n== 2. what will be committed ==" -ForegroundColor Cyan
git add -A
git status --short
Write-Host ""
if ((Read-Host "Commit and push the above? (y/n)") -ne "y") {
    Write-Host "Stopped. Nothing was pushed." -ForegroundColor Yellow; exit 1
}

Write-Host "`n== 3. committing ==" -ForegroundColor Cyan
git commit -m "release: target API 36, fastlane metadata, public signed APK releases, doc restructure"

Write-Host "`n== 4. pushing main ==" -ForegroundColor Cyan
git push

Write-Host "`n== 5. tag $tag ==" -ForegroundColor Cyan
Write-Host "Pushing this tag triggers .github/workflows/release.yml, which needs these" -ForegroundColor Yellow
Write-Host "GitHub Actions secrets: KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD." -ForegroundColor Yellow
Write-Host "Without them the run fails and $tag is spent." -ForegroundColor Yellow
Write-Host "  https://github.com/nikolozturkishvili1/Touchgrass/settings/secrets/actions" -ForegroundColor Yellow
Write-Host ""
if ((Read-Host "Have you confirmed all four secrets are set? (y/n)") -ne "y") {
    Write-Host "Stopped before tagging. main is pushed; rerun once the secrets exist." -ForegroundColor Yellow
    exit 0
}

if (git tag -l $tag) { throw "Tag $tag already exists locally. Bump versionCode/versionName instead of re-tagging." }
git tag $tag
git push origin $tag

Write-Host "`nDone. Watch the build:" -ForegroundColor Green
Write-Host "  https://github.com/nikolozturkishvili1/Touchgrass/actions"
Write-Host "Then confirm the APK is attached AND the release is not a draft:" -ForegroundColor Green
Write-Host "  https://github.com/nikolozturkishvili1/Touchgrass/releases/latest"
