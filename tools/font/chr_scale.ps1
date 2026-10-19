# chr_scale.ps1 - double the per-glyph UV rects in Haegemonia .chr font files
# .chr format: "INFO" + size(u32) + nameCount(u32) + per name: len(u32)+bytes
#              + 4 x u32 (cellW, cellH, firstChar, recordCount)
#              + recordCount x 10 bytes (5 x u16: advance, u0, v0, u1, v1)
# Scales u0,v0,u1,v1 by SCALE (2.0). Advance is left unchanged.
param(
    [Parameter(Mandatory=$true)][string[]]$Files,
    [double]$Scale = 2.0
)

foreach ($f in $Files) {
    if (-not (Test-Path -LiteralPath $f)) { Write-Output "MISSING: $f"; continue }
    $b = [System.IO.File]::ReadAllBytes($f)
    $ok = $true
    if ([System.Text.Encoding]::ASCII.GetString($b,0,4) -ne "INFO") { Write-Output "BAD MAGIC: $f"; continue }
    $size = [BitConverter]::ToUInt32($b,4)
    if ($size -ne $b.Length) { Write-Output "SIZE MISMATCH ($size vs $($b.Length)): $f" }
    $nameCount = [BitConverter]::ToUInt32($b,8)
    $off = 12
    for ($i = 0; $i -lt $nameCount; $i++) {
        $len = [BitConverter]::ToUInt32($b,$off)
        $off += 4 + $len
    }
    $cellW  = [BitConverter]::ToUInt32($b,$off)
    $cellH  = [BitConverter]::ToUInt32($b,$off+4)
    $first  = [BitConverter]::ToUInt32($b,$off+8)
    $count  = [BitConverter]::ToUInt32($b,$off+12)
    $off += 16
    Write-Output ("{0}: names={1} cell={2}x{3} first={4} records={5} dataEnd=0x{6:X} fileLen=0x{7:X}" -f `
        (Split-Path $f -Leaf), $nameCount, $cellW, $cellH, $first, $count, ($off + $count*10), $b.Length)
    if (($off + $count*10) -ne $b.Length) {
        Write-Output "  WARNING: record area does not end at EOF - skipping $f"
        continue
    }
    $changed = 0
    for ($i = 0; $i -lt $count; $i++) {
        $r = $off + $i*10
        foreach ($o in @(2,4,6,8)) {
            $v = [BitConverter]::ToUInt16($b, $r + $o)
            $nv = [int]([Math]::Round($v * $Scale))
            if ($nv -gt 65535) { $nv = 65535 }
            if ($nv -ne $v) {
                $b[$r+$o] = [byte]($nv -band 0xFF)
                $b[$r+$o+1] = [byte](($nv -shr 8) -band 0xFF)
                $changed++
            }
        }
    }
    if ($changed -gt 0) {
        $bak = "$f.bak1x"
        if (-not (Test-Path -LiteralPath $bak)) {
            Copy-Item -LiteralPath $f -Destination $bak
        }
        [System.IO.File]::WriteAllBytes($f, $b)
        Write-Output "  SCALED x$Scale : $changed values changed, backup -> $bak"
    } else {
        Write-Output "  no change (already scaled?)"
    }
}
