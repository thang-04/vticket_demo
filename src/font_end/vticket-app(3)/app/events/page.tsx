import { Header } from "@/components/header"
import { CategoryFilter } from "@/components/category-filter"
import { EventCard } from "@/components/event-card"
import { Button } from "@/components/ui/button"
import { ChevronDown } from "lucide-react"

// Mock data for events
const events = [
  {
    id: "1",
    title: "THE WANDERING ROSE DAY 3",
    date: "27/09/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "1,000,000 VND",
    image: "/placeholder-b6f6y.png",
    category: "Âm nhạc",
  },
  {
    id: "2",
    title: "Sao Nhập Ngũ Concert 2025",
    date: "24/08/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "700,000 VND",
    image: "/placeholder-i5exo.png",
    category: "Giải trí",
  },
  {
    id: "3",
    title: "Musique De Salon 17 - TP HCM: Để Nhớ Một Thời",
    date: "02/08/2025",
    location: "Nhà hát Thành phố",
    price: "900,000 VND",
    image: "/placeholder-dy13l.png",
    category: "Âm nhạc",
  },
  {
    id: "4",
    title: "Musique De Salon 17 - Hà Nội: Để Nhớ Một Thời",
    date: "16/08/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "900,000 VND",
    image: "/placeholder-uwse8.png",
    category: "Âm nhạc",
  },
  {
    id: "5",
    title: "The Wandering Rose 2/8",
    date: "02/08/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "800,000 VND",
    image: "/placeholder-1zitm.png",
    category: "Âm nhạc",
  },
  {
    id: "6",
    title: "The Men Live Tour | Cảm Xúc Trở Lại",
    date: "19/07/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "1,000,000 VND",
    image: "/placeholder-kjsfm.png",
    category: "Âm nhạc",
  },
  {
    id: "7",
    title: "The Wandering Rose",
    date: "26/07/2025",
    location: "Cung Văn hóa Hữu nghị Việt - Xô",
    price: "800,000 VND",
    image: "/placeholder-wj8va.png",
    category: "Âm nhạc",
  },
  {
    id: "8",
    title: "Khóa Đào Tạo Thợ Làm Bánh Chuyên Nghiệp",
    date: "15/08/2025",
    location: "Trung tâm Đào tạo",
    price: "2,500,000 VND",
    image: "/placeholder-5ljj4.png",
    category: "Giáo dục",
  },
  {
    id: "9",
    title: "Musique De Salon 16: Âm Nhạc & Điện Ảnh",
    date: "12/09/2025",
    location: "Nhà hát Thành phố",
    price: "850,000 VND",
    image: "/placeholder-v23ht.png",
    category: "Âm nhạc",
  },
]

export default function EventsPage() {
  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container mx-auto px-4 py-6">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-6">
          <span>Trang chủ</span>
          <span>{">"}</span>
          <span className="text-foreground">Khám phá dịch vụ</span>
        </div>

        <div className="flex gap-6">
          {/* Sidebar Filter */}
          <CategoryFilter />

          {/* Main Content */}
          <div className="flex-1">
            <div className="flex items-center justify-between mb-6">
              <h1 className="text-xl font-semibold text-foreground">Kết quả tìm kiếm cho từ khóa:</h1>
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">Sắp xếp theo:</span>
                <Button variant="outline" className="bg-card border-border text-card-foreground">
                  Mới nhất
                  <ChevronDown className="w-4 h-4 ml-2" />
                </Button>
              </div>
            </div>

            {/* Events Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {events.map((event) => (
                <EventCard key={event.id} {...event} />
              ))}
            </div>

            {/* Load More */}
            <div className="flex justify-center mt-8">
              <Button variant="outline" className="bg-card border-border text-card-foreground">
                Xem thêm sự kiện
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
