package com.jcorp.jaygauge

import androidx.core.graphics.toColorInt

enum class GaugeArcColorTheme(val colors: IntArray, val themeName: String, val id: Int) {

    Default(
        themeName = "Default",
        id = 0,
        colors = intArrayOf(
            "#00FF00".toColorInt(), // bright green
            "#7CFC00".toColorInt(), // lawn green
            "#ADFF2F".toColorInt(), // green-yellow
            "#FFFF00".toColorInt(), // yellow
            "#FFD700".toColorInt(), // gold
            "#E75116".toColorInt(), // orange
            "#EB0505".toColorInt()  // bright red
        )
    ),
    Ryzen(
        themeName = "Ryzen",
        id = 35,
        colors = intArrayOf(
            "#B31A1C".toColorInt(), // Red
            "#A1181A".toColorInt(), // RED-Mid-scale
            "#B22D16".toColorInt(), // Mid-Red
            "#E46F0D".toColorInt(), // orange
            "#DC630C".toColorInt(), // orange-brighter
            "#DC630C".toColorInt(), // orange-brighter
            "#BA3816".toColorInt(), // orange-darker
        )
    ),
    Nvidia(
        themeName = "Nvidia",
        id = 37,
        colors = intArrayOf(
            "#C0F068".toColorInt(),  // dark green
            "#A4EB22".toColorInt(), // moss green
            "#A4EB22".toColorInt(), // light lime
            "#89CE0A".toColorInt(), // bright green
            "#89CE0A".toColorInt(), // lime green
            "#74B200".toColorInt(), // green
            "#74B200".toColorInt() // strong green
        )
    ),
    Intel(
        themeName = "Intel",
        id = 38,
        colors = intArrayOf(
            "#9F5FC4".toColorInt(),
            "#8756BF".toColorInt(),
            "#7795D4".toColorInt(),
            "#5499DD".toColorInt(),
            "#5DBCEE".toColorInt(),
            "#6ED8EF".toColorInt(),
            "#1355BA".toColorInt()
        )
    ),

    OceanBreeze(
        themeName = "OceanBreeze",
        id = 1,
        colors = intArrayOf(
            "#00C3FF".toColorInt(), // cyan
            "#1A9EE8".toColorInt(), // cyan-blue
            "#3A7BD5".toColorInt(), // blue
            "#225FA4".toColorInt(), // deep blue
            "#004E92".toColorInt(), // navy
            "#002F6C".toColorInt(), // deep navy
            "#002554".toColorInt()  // midnight
        )
    ),

    Sunset(
        themeName = "Sunset",
        id = 2,
        colors = intArrayOf(
            "#FF512F".toColorInt(), // orange red
            "#FF7043".toColorInt(), // warm orange
            "#FF8A65".toColorInt(), // coral
            "#F23846".toColorInt(), // pinkish magenta
            "#DD2476".toColorInt(), // magenta
            "#8B008B".toColorInt(), // dark magenta
            "#4B0082".toColorInt()  // indigo
        )
    ),

    Aurora(
        themeName = "Aurora",
        id = 3,
        colors = intArrayOf(
            "#00F260".toColorInt(), // neon green
            "#00E37C".toColorInt(), // mint green
            "#02AA94".toColorInt(), // teal
            "#04C8C8".toColorInt(), // aqua
            "#0575E6".toColorInt(), // blue
            "#023E73".toColorInt(), // deep blue
            "#011F3F".toColorInt()  // dark navy
        )
    ),

    CitrusPunch(
        themeName = "CitrusPunch",
        id = 4,
        colors = intArrayOf(
            "#F7971E".toColorInt(), // orange
            "#FFA500".toColorInt(), // orange
            "#FFB600".toColorInt(), // orange-yellow
            "#FFD200".toColorInt(), // yellow
            "#FFF200".toColorInt(), // bright yellow
            "#FFD700".toColorInt(), // gold
            "#8B4513".toColorInt()  // saddle brown
        )
    ),

    NeonParty(
        themeName = "NeonParty",
        id = 5,
        colors = intArrayOf(
            "#00FFFF".toColorInt(), // cyan
            "#00BFFF".toColorInt(), // deep sky blue
            "#8A2BE2".toColorInt(), // blue violet
            "#FF00FF".toColorInt(), // magenta
            "#FF1493".toColorInt(), // neon pink
            "#FF69B4".toColorInt(), // hot pink
            "#8B008B".toColorInt()  // dark magenta
        )
    ),

    RoyalPurple(
        themeName = "RoyalPurple",
        id = 6,
        colors = intArrayOf(
            "#654EA3".toColorInt(), // purple
            "#7B68EE".toColorInt(), // slate blue
            "#8A2BE2".toColorInt(), // blue violet
            "#9932CC".toColorInt(), // dark orchid
            "#BA55D3".toColorInt(), // orchid
            "#9400D3".toColorInt(), // dark violet
            "#4B0082".toColorInt()  // indigo
        )
    ),
    MintWave(
        themeName = "MintWave",
        id = 16,
        colors = intArrayOf(
            "#00C3FF".toColorInt(), // cyan blue
            "#33EFFF".toColorInt(), // bright aqua
            "#66FFE0".toColorInt(), // mint
            "#66FF99".toColorInt(), // mint green
            "#33CC66".toColorInt(), // green
            "#009966".toColorInt(), // deep teal
            "#006655".toColorInt()  // dark teal
        )
    ),

    RoyalMystic(
        themeName = "RoyalMystic",
        id = 17,
        colors = intArrayOf(
            "#3F00FF".toColorInt(), // royal blue
            "#6200EA".toColorInt(), // deep indigo
            "#7B1FA2".toColorInt(), // mystic purple
            "#9C27B0".toColorInt(), // vibrant purple
            "#AB47BC".toColorInt(), // lavender purple
            "#6A1B9A".toColorInt(), // deep purple
            "#4A148C".toColorInt()  // darkest purple
        )
    ),

    AquaFusion(
        themeName = "AquaFusion",
        id = 18,
        colors = intArrayOf(
            "#3F51B5".toColorInt(), // indigo
            "#2196F3".toColorInt(), // blue
            "#00BCD4".toColorInt(), // cyan
            "#4DD0E1".toColorInt(), // light teal
            "#26C6DA".toColorInt(), // teal
            "#0097A7".toColorInt(), // deep teal
            "#006064".toColorInt()  // darkest teal
        )
    ),

    DeepOcean(
        themeName = "DeepOcean",
        id = 19,
        colors = intArrayOf(
            "#1A237E".toColorInt(), // deep navy
            "#283593".toColorInt(), // dark blue
            "#303F9F".toColorInt(), // ocean blue
            "#3949AB".toColorInt(), // blue
            "#3F51B5".toColorInt(), // bright blue
            "#1E3A8A".toColorInt(), // deep blue
            "#0D47A1".toColorInt()  // darkest blue
        )
    ),

    MintLagoon(
        themeName = "MintLagoon",
        id = 20,
        colors = intArrayOf(
            "#00BCD4".toColorInt(), // cyan
            "#00E5FF".toColorInt(), // bright aqua
            "#18FFFF".toColorInt(), // neon mint
            "#64FFDA".toColorInt(), // mint green
            "#1DE9B6".toColorInt(), // greenish mint
            "#00BFA5".toColorInt(), // teal green
            "#004D40".toColorInt()  // dark teal
        )
    ),

    PurpleBliss(
        themeName = "PurpleBliss",
        id = 21,
        colors = intArrayOf(
            "#AB47BC".toColorInt(), // purple
            "#7E57C2".toColorInt(), // violet
            "#5C6BC0".toColorInt(), // soft blue
            "#42A5F5".toColorInt(), // sky blue
            "#29B6F6".toColorInt(), // light blue
            "#1565C0".toColorInt(), // deep blue
            "#0D47A1".toColorInt()  // darkest blue
        )
    ),

    CitrusSunrise(
        themeName = "CitrusSunrise",
        id = 22,
        colors = intArrayOf(
            "#FF4081".toColorInt(), // pink
            "#FF7043".toColorInt(), // coral
            "#FFA726".toColorInt(), // orange
            "#FFEB3B".toColorInt(), // yellow
            "#FFF176".toColorInt(), // light yellow
            "#FFC107".toColorInt(), // amber
            "#FF6F00".toColorInt()  // deep amber
        )
    ),

    FlamingoGlow(
        themeName = "FlamingoGlow",
        id = 23,
        colors = intArrayOf(
            "#FF4081".toColorInt(), // pink
            "#FF5252".toColorInt(), // bright red-pink
            "#FF1744".toColorInt(), // neon red
            "#FF3D00".toColorInt(), // red-orange
            "#FF5722".toColorInt(), // coral
            "#D84315".toColorInt(), // burnt orange
            "#BF360C".toColorInt()  // dark burnt orange
        )
    ),
    CoralTwist(
        themeName = "CoralTwist",
        id = 24,
        colors = intArrayOf(
            "#FF5722".toColorInt(), // coral
            "#FF7043".toColorInt(), // light coral
            "#FF8A65".toColorInt(), // peach
            "#FF4081".toColorInt(), // magenta pink
            "#F50057".toColorInt(), // neon pink
            "#C51162".toColorInt(), // dark magenta
            "#880E4F".toColorInt()  // deep wine
        )
    ),

    SunsetPink(
        themeName = "SunsetPink",
        id = 25,
        colors = intArrayOf(
            "#FF5722".toColorInt(), // coral orange
            "#FF3D63".toColorInt(), // strong pink
            "#FF1493".toColorInt(), // deep pink
            "#FF00C8".toColorInt(), // neon pink
            "#E040FB".toColorInt(), // bright magenta
            "#8E24AA".toColorInt(), // violet
            "#4A148C".toColorInt()  // dark violet
        )
    ),

    PurpleHorizon(
        themeName = "PurpleHorizon",
        id = 26,
        colors = intArrayOf(
            "#3F51B5".toColorInt(), // indigo
            "#673AB7".toColorInt(), // purple
            "#9C27B0".toColorInt(), // vibrant purple
            "#E040FB".toColorInt(), // bright magenta
            "#BA68C8".toColorInt(), // lavender
            "#8E24AA".toColorInt(), // deep violet
            "#4A148C".toColorInt()  // darkest violet
        )
    ),

    OrangeBlaze(
        themeName = "OrangeBlaze",
        id = 27,
        colors = intArrayOf(
            "#FF5722".toColorInt(), // orange
            "#FF8F00".toColorInt(), // amber orange
            "#FFA000".toColorInt(), // golden amber
            "#FFB300".toColorInt(), // light amber
            "#FFC107".toColorInt(), // yellow amber
            "#FF6F00".toColorInt(), // deep amber
            "#E65100".toColorInt()  // burnt amber
        )
    ),

    PinkBlush(
        themeName = "PinkBlush",
        id = 28,
        colors = intArrayOf(
            "#E91E63".toColorInt(), // pink
            "#FF4081".toColorInt(), // vibrant pink
            "#FF6E97".toColorInt(), // soft pink
            "#FF8A80".toColorInt(), // coral pink
            "#FF5252".toColorInt(), // pink-red
            "#D81B60".toColorInt(), // deep pink
            "#880E4F".toColorInt()  // dark pink
        )
    ),

    AquaWave(
        themeName = "AquaWave",
        id = 29,
        colors = intArrayOf(
            "#8E24AA".toColorInt(), // purple
            "#7B1FA2".toColorInt(), // deep purple
            "#3F51B5".toColorInt(), // blue
            "#03A9F4".toColorInt(), // sky blue
            "#00BCD4".toColorInt(), // cyan
            "#0097A7".toColorInt(), // teal
            "#004D40".toColorInt()  // deep teal
        )
    ),

    TealMist(
        themeName = "TealMist",
        id = 30,
        colors = intArrayOf(
            "#00BCD4".toColorInt(), // cyan
            "#4DD0E1".toColorInt(), // light cyan
            "#80DEEA".toColorInt(), // pale mint
            "#A7FFEB".toColorInt(), // mint green
            "#69F0AE".toColorInt(), // light green
            "#26A69A".toColorInt(), // teal
            "#004D40".toColorInt()  // deep teal
        )
    ),

    LavenderMist(
        themeName = "LavenderMist",
        id = 36,
        colors = intArrayOf(
            "#00BCD4".toColorInt(), // cyan
            "#4DD0E1".toColorInt(), // light cyan
            "#4DD0E1".toColorInt(), // pale mint
            "#A7FFEB".toColorInt(), // mint green
            "#69F0AE".toColorInt(), // light green
            "#EDAEF2".toColorInt(), // pink
            "#F1CCF4".toColorInt()  // pink light
        )
    ),

    VioletGlow(
        themeName = "VioletGlow",
        id = 31,
        colors = intArrayOf(
            "#3F51B5".toColorInt(), // indigo
            "#673AB7".toColorInt(), // purple
            "#9C27B0".toColorInt(), // vibrant purple
            "#E040FB".toColorInt(), // magenta
            "#F8BBD0".toColorInt(), // soft pink
            "#8E24AA".toColorInt(), // deep purple
            "#4A148C".toColorInt()  // darkest purple
        )
    ),

    SunsetFire(
        themeName = "SunsetFire",
        id = 32,
        colors = intArrayOf(
            "#FFEB3B".toColorInt(), // yellow
            "#FFC107".toColorInt(), // amber
            "#FF9800".toColorInt(), // orange
            "#FF5722".toColorInt(), // deep orange
            "#F44336".toColorInt(), // red
            "#D32F2F".toColorInt(), // deep red
            "#B71C1C".toColorInt()  // darkest red
        )
    ),

    GreenPulse(
        themeName = "GreenPulse",
        id = 33,
        colors = intArrayOf(
            "#00E676".toColorInt(), // neon green
            "#00C853".toColorInt(), // strong green
            "#00FF00".toColorInt(), // bright green
            "#76FF03".toColorInt(), // lime green
            "#B2FF59".toColorInt(), // light lime
            "#558B2F".toColorInt(), // moss green
            "#33691E".toColorInt()  // dark green
        )
    ),

    MintSky(
        themeName = "MintSky",
        id = 34,
        colors = intArrayOf(
            "#00BCD4".toColorInt(), // cyan
            "#4DD0E1".toColorInt(), // light cyan
            "#A7FFEB".toColorInt(), // mint green
            "#B2EBF2".toColorInt(), // light mint
            "#E0F7FA".toColorInt(), // very light mint
            "#00ACC1".toColorInt(), // teal blue
            "#006064".toColorInt()  // deep teal
        )
    ),


}
