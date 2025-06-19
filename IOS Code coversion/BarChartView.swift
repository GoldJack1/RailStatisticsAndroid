import SwiftUI


struct BarChartView: View {
    let data: [(label: String, value: Double, color: Color)]
    let title: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ForEach(sortedData(), id: \.label) { item in
                VStack(alignment: .leading, spacing: 4) { // Group label and bar
                    Text(item.label)
                        .font(.system(size: 14, weight: .semibold)) // Adjust font size for readability
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading) // Allow multi-line text
                        .lineLimit(2) // Limit to 2 lines
                        .truncationMode(.tail) // Handle text overflow gracefully

                    GeometryReader { geometry in
                        ZStack(alignment: .leading) { // Use ZStack to overlay text on the bar
                            RoundedRectangle(cornerRadius: 8)
                                .fill(item.color)
                                .frame(width: geometry.size.width * CGFloat(item.value / maxValue()), height: 20)

                            Text("\(Int(item.value))") // Place the number inside the bar
                                .font(.footnote)
                                .foregroundColor(.white) // Use contrasting color for visibility
                                .padding(.leading, 8) // Add padding inside the bar
                        }
                    }
                    .frame(height: 20) // Consistent bar height
                }
                .padding(.bottom, 8) // Adds consistent spacing between bars
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 3)
    }

    private func maxValue() -> Double {
        data.map { $0.value }.max() ?? 1
    }

    // Sort data by value in descending order
    private func sortedData() -> [(label: String, value: Double, color: Color)] {
        data.sorted { $0.value > $1.value }
    }
}
