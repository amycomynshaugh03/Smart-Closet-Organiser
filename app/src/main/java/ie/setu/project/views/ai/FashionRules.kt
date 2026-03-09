package ie.setu.project.views.ai

object FashionRules {

    val generalRules = listOf(
        "NEVER pair navy blue and black together — they are too similar and look like a mismatched accident.",
        "NEVER pair two different shades of black together — faded black and true black look like a mistake.",
        "Avoid pairing two different shades of the same colour unless they are very deliberately contrasted.",
        "Bright orange and bright pink together is overwhelming — avoid.",
        "Avoid red and pink unless they are very distinctly different tones (e.g. deep red + hot pink).",
        "Avoid mixing warm whites and cool whites in the same outfit — off-white and bright white clash.",

        "Navy and white is a timeless pairing — always recommend it when available.",
        "Black and white always works — a safe, stylish combination.",
        "Navy and grey is a reliable, clean combination.",
        "Black and camel or black and tan is a strong classic pairing.",
        "White and earth tones (tan, brown, rust, olive, cream) always look clean and balanced.",

        "Earth tones (brown, tan, cream, rust, olive, terracotta) combine beautifully together.",
        "Cool tones (navy, grey, white, black, cobalt) work well together.",
        "Avoid mixing warm-toned and cool-toned neutrals — e.g. a warm beige with a cool grey can look off.",
        "A neutral base (black, white, grey, beige, navy) with one pop of colour is always safe and stylish.",

        "Avoid wearing two bold or busy patterns at the same time — one pattern per outfit.",
        "If mixing patterns, one must be significantly smaller in scale than the other.",
        "Stripes and checks can work together only if they differ greatly in scale and colour.",

        "Avoid wearing two oversized or very baggy pieces together — balance with at least one fitted item.",
        "If the top is voluminous, the bottom should be slim — and vice versa.",

        "Match formality levels — do not pair a blazer with athletic joggers or a hoodie.",
        "Shoes must match the formality of the outfit — avoid running shoes with formal trousers or dresses.",
        "Denim is casual — avoid pairing it with formal or business attire.",

        "Belt colour should match or closely complement shoe colour.",
        "Do not mix gold and silver accessories in the same outfit.",
        "Avoid mixing leather and suede in shoes and bags within the same outfit.",
        "Socks should match the trousers, not the shoes, when wearing formal or smart outfits.",

        "Denim works with almost everything but looks best paired with neutrals or a single solid colour.",
        "Suggest layering options when the temperature is between 10°C and 18°C.",
        "Avoid heavy fabrics like wool or tweed in warm weather — opt for linen, cotton, or light blends.",
    )

    fun weatherRules(weatherCode: Int, tempCelsius: Float): List<String> {
        val rules = mutableListOf<String>()

        // Drizzle or rain showers
        if (weatherCode in 51..67 || weatherCode in 80..82) {
            rules += "It is RAINING — strongly prioritise outerwear with a hood (hooded jacket, anorak, parka) over an umbrella-dependent option."
            rules += "Suggest waterproof or water-resistant footwear — avoid suede, canvas, or open-toe shoes."
            rules += "Avoid light fabrics like linen or silk that become see-through or clingy when wet."
            rules += "Darker colours are preferable in rain as watermarks show less."
        }

        // Heavy rain or thunderstorm
        if (weatherCode in 61..67 || weatherCode >= 95) {
            rules += "It is HEAVILY RAINING or STORMY — a full waterproof jacket is essential, not optional."
            rules += "Avoid wearing anything delicate or hard to dry."
        }

        // Snow
        if (weatherCode in 71..77 || weatherCode in 85..86) {
            rules += "It is SNOWING — suggest warm waterproof boots if available."
            rules += "Recommend layering with thermal or wool base layers."
            rules += "Avoid anything with an open hem at the ankle that could get wet and cold."
        }

        // Cold
        if (tempCelsius < 8f) {
            rules += "It is COLD (below 8°C) — a coat or heavy jacket is essential, not optional."
            rules += "Suggest layering: base layer + mid layer (jumper or cardigan) + outer coat."
            if (tempCelsius < 5f) rules += "It is very cold — mention a scarf and hat."
        }

        // Cool
        if (tempCelsius in 8f..15f) {
            rules += "It is COOL (8–15°C) — a light jacket, cardigan, or denim jacket is recommended."
            rules += "Long sleeves are preferable over short sleeves."
        }

        // Warm
        if (tempCelsius > 22f) {
            rules += "It is WARM (above 22°C) — suggest lighter fabrics like cotton, linen, or jersey."
            rules += "Lighter colours reflect heat better — whites, creams, and pastels are great choices."
            rules += "Heavy knits, wool, or thick denim are not ideal in warm weather."
        }

        return rules
    }

    fun buildRulesPrompt(weatherCode: Int, tempCelsius: Float): String {
        val all = generalRules + weatherRules(weatherCode, tempCelsius)
        return all.joinToString("\n") { "- $it" }
    }
}