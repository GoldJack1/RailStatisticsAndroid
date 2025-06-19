import SwiftUI

// Helper for rounded corners on specific edges
struct RoundedCorners: View {
    var color: Color
    var tl: CGFloat = 0.0
    var tr: CGFloat = 0.0
    var bl: CGFloat = 0.0
    var br: CGFloat = 0.0
    var body: some View {
        GeometryReader { geometry in
            Path { path in
                let w = geometry.size.width
                let h = geometry.size.height
                // Ensure radii don't exceed bounds
                let tr = min(min(self.tr, h/2), w/2)
                let tl = min(min(self.tl, h/2), w/2)
                let bl = min(min(self.bl, h/2), w/2)
                let br = min(min(self.br, h/2), w/2)
                path.move(to: CGPoint(x: w / 2.0, y: 0))
                path.addLine(to: CGPoint(x: w - tr, y: 0))
                path.addArc(center: CGPoint(x: w - tr, y: tr), radius: tr,
                            startAngle: Angle(degrees: -90), endAngle: Angle(degrees: 0), clockwise: false)
                path.addLine(to: CGPoint(x: w, y: h - br))
                path.addArc(center: CGPoint(x: w - br, y: h - br), radius: br,
                            startAngle: Angle(degrees: 0), endAngle: Angle(degrees: 90), clockwise: false)
                path.addLine(to: CGPoint(x: bl, y: h))
                path.addArc(center: CGPoint(x: bl, y: h - bl), radius: bl,
                            startAngle: Angle(degrees: 90), endAngle: Angle(degrees: 180), clockwise: false)
                path.addLine(to: CGPoint(x: 0, y: tl))
                path.addArc(center: CGPoint(x: tl, y: tl), radius: tl,
                            startAngle: Angle(degrees: 180), endAngle: Angle(degrees: 270), clockwise: false)
            }
            .fill(self.color)
        }
    }
}

// Font variation helper
extension View {
    func fontVariation(crsv: Int, wght: Int) -> some View {
        self.modifier(FontVariationModifier(crsv: crsv, wght: wght))
    }
}

struct FontVariationModifier: ViewModifier {
    var crsv: Int
    var wght: Int
    func body(content: Content) -> some View {
        content
            .environment(\.font, Font.custom("Geologica", size: UIFont.labelFontSize).weight(fontWeight(for: wght)))
    }
    private func fontWeight(for wght: Int) -> Font.Weight {
        switch wght {
        case ..<400: return .ultraLight
        case 400: return .regular
        case 500: return .medium
        case 600: return .semibold
        case 700: return .bold
        case 800: return .heavy
        case 900...: return .black
        default: return .regular
        }
    }
} 