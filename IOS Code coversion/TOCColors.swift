import SwiftUI

// Dictionary mapping TOC names to their HEX color codes
let tocColors: [String: String] = [
    "Standard": "#868686",
    "First": "#9800F0",
    "Greater Anglia": "#d70428",
    "ScotRail": "#1e467d",
    "Avanti West Coast": "#004354",
    "c2c": "#b7007c",
    "Caledonian Sleeper": "#1d2e35",
    "Chiltern Railways": "#00bfff",
    "CrossCountry": "#660f21",
    "East Midlands Railway": "#713563",
    "Great Western Railway": "#0a493e",
    "Hull Trains": "#de005c",
    "Thameslink/Great Northern": "#ff5aa4",
    "Heathrow Express": "#532e63",
    "Island Line": "#1e90ff",
    "Transport for Wales": "#ff0000",
    "LNER": "#ce0e2d",
    "Northern": "#034DE2",
    "TransPennine Express": "#09a4ec",
    "Multi-Operator": "#F1F1F1",
    "Merseyrail": "#fff200",
    "Gatwick Express": "#EB1E2D",
    "Great Northern": "#ff5aa4",
    "LNWR": "#00bf6f",
    "South Western Railway": "#24398c",
    "Southeastern": "#389cff",
    "Southern": "#8cc63e",
    "Thameslink": "#ff5aa4",
    "West Midlands Trains": "#ff8300",
    "Lumo": "#2b6ef5",
    "Grand Central": "#1d1d1b",
    "London Overground": "#e87722",
    "London Underground": "#10069f",
    "Elizabeth Line": "#6950a1",
    "Bee Network": "#FFCC33",
    "Blackpool Tramway": "#7F2680",
    "Docklands Light Railway (DLR)": "#00b2a9",
    "Edinburgh Trams": "#8D122A",
    "Glasgow Subway": "#f57c14",
    "London Tramlink": "#78be20",
    "Manchester Metrolink": "#edb600",
    "Nottingham Express Transit (NET)": "#01796f",
    "Sheffield Supertram": "#C67A1E",
    "Tyne & Wear Metro": "#FBB10F",
    "West Midlands Metro": "#0075c9",
    "Heritage": "#800000",
    "International": "#355e3b",
    "Eurostar": "#086bfe",
    // Add more TOCs and their colors as needed
]

// Extension to initialize Color with a HEX string
extension Color {
    init?(hex: String) {
        var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")

        var rgb: UInt64 = 0
        guard Scanner(string: hexSanitized).scanHexInt64(&rgb) else { return nil }

        let red = Double((rgb >> 16) & 0xFF) / 255.0
        let green = Double((rgb >> 8) & 0xFF) / 255.0
        let blue = Double(rgb & 0xFF) / 255.0

        self.init(red: red, green: green, blue: blue)
    }
}

// Function to get a Color for a TOC
func colorForTOC(_ toc: String?) -> Color? {
    guard let toc = toc, let hex = tocColors[toc] else {
        return nil // Return nil if TOC or HEX code is not found
    }
    return Color(hex: hex)
}
