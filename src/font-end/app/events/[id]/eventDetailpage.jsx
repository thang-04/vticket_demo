import { Header } from "@/components/header"
import { Button } from "@/components/ui/button"
import { Calendar, MapPin, Share2, Heart } from "lucide-react"
import Image from "next/image"

// Mock data for event detail
const eventData = {
  id: "2",
  title: "Sao Nhập Ngũ Concert 2025",
  venue: "Cung Văn hóa Sáng Tạo",
  address: "Đường Lương Định Của, phường An Khánh, TP. HCM",
  date: "24/08/2025",
  time: "18H",
  price: "700,000 VND",
  image: "/placeholder-s6dnt.png",
  description: `Sao Nhập Ngũ đã trở thành biểu tượng sống động trong trái tim khán giả cả nước từ những thuở phim khởi chiếu long trọng cách đây kỷ luật thép và tình thần kiên cường của người lính. Vượt ra khỏi một show truyền hình, hành trình "Sao Nhập Ngũ" sẽ chính thức bước lên sân khấu âm nhạc với Sao Nhập Ngũ Concert hướng đến kỷ niệm 80 năm Quốc khánh 2/9.

💚 Chương trình âm nhạc SAO NHẬP NGŨ CONCERT 2025 quy tụ dàn nghệ sĩ hùng hậu Ca sĩ, Hoa Hậu, Diễn Viên, Người Mẫu: SOOBIN, TRÚC NHÂN, HOÀ MINZY, CHUNG GIANG, LAN NGỌC, KỲ DUYÊN, TRANG PHÁP, BÙI CÔNG NAM, THÀNH DUY, JUN PHẠM, DƯƠNG HOÀNG YẾN, TĂNG PHÚC, HUỲNH LẬP, DUY KHÁNH, LYLY, DOUBLE2T, PHÁO, HẬU HOÀNG, CARA, HẢI ĐĂNG DÔO, LINH NGỌC ĐÀM, MAYDA's

💚 Vai trò Bảo điện và Giám đốc Âm nhạc được đảm nhiệm bởi "Bộ đôi hoàn hảo" ĐINH HÀ UYÊN THU và SLIMV

💚 Chương trình hứa hẹn là trải nghiệm "có một không hai" phong cách "không dừng hàng" của Sao Nhập Ngũ:
🎵 Những HIT triệu view của thế hệ trẻ được dàn dựng mới mẻ
🎵 Những ca khúc "đi cùng năm tháng" được phối mới theo âm hưởng hiện đại
🎵 Một câu chuyện âm nhạc xuyên suốt lịch sử – chạm vào lòng tự hào của những trái tim Việt Nam`,
}

export default function EventDetailPage({ params }) {
  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container mx-auto px-4 py-6">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-6">
          <span>Trang chủ</span>
          <span>{">"}</span>
          <span className="text-foreground">{eventData.title}</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Content */}
          <div className="lg:col-span-2 space-y-6">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-4">{eventData.title}</h1>

              <div className="space-y-3 text-muted-foreground">
                <div className="flex items-center gap-2">
                  <MapPin className="w-5 h-5" />
                  <div>
                    <div className="font-medium text-foreground">{eventData.venue}</div>
                    <div className="text-sm">{eventData.address}</div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Calendar className="w-5 h-5" />
                  <span>Thời gian: {eventData.date}</span>
                </div>
              </div>

              <div className="mt-6">
                <div className="text-lg font-semibold text-foreground mb-2">
                  Giá chỉ từ <span className="text-green-500">{eventData.price}</span>
                </div>
                <div className="flex gap-3">
                  <Button className="bg-gray-600 hover:bg-gray-700 text-white px-8" disabled>
                    Đã ngừng bán
                  </Button>
                  <Button variant="outline" size="icon" className="border-border bg-transparent">
                    <Share2 className="w-4 h-4" />
                  </Button>
                  <Button variant="outline" size="icon" className="border-border bg-transparent">
                    <Heart className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </div>

            {/* Description */}
            <div className="bg-card rounded-lg p-6">
              <h2 className="text-xl font-semibold text-card-foreground mb-4">Giới thiệu</h2>
              <div className="text-muted-foreground whitespace-pre-line leading-relaxed">{eventData.description}</div>
            </div>
          </div>

          {/* Right Sidebar - Event Image */}
          <div className="lg:col-span-1">
            <div className="sticky top-6">
              <div className="relative aspect-[3/4] rounded-lg overflow-hidden">
                <Image
                  src={eventData.image || "/placeholder.svg"}
                  alt={eventData.title}
                  fill
                  className="object-cover"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
