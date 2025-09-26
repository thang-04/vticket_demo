import React from 'react';
import { Calendar, MapPin } from 'lucide-react';

// Định nghĩa kiểu dữ liệu cho props của component
interface EventCardProps {
  id: number;
  title: string;
  startTime: string;
  venue: string;
  price: number;
  image: string | null;
  categoryName: string;
}

export function EventCard({
  id,
  title,
  startTime,
  venue,
  price,
  image,
  categoryName,
}: EventCardProps) {
  // Hàm để định dạng chuỗi ngày tháng
  const formatDate = (dateString: string): string => {
    if (!dateString) return "N/A";
    try {
      const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' };
      const date = new Date(dateString);
      return date.toLocaleDateString("vi-VN", options);
    } catch (error) {
      console.error("Error formatting date:", error);
      return "Invalid Date";
    }
  };

  // Hàm để định dạng giá tiền
  const formatPrice = (priceValue: number): string => {
    if (typeof priceValue !== 'number') return "Free";
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(priceValue);
  };

  return (
    // Trong một ứng dụng Next.js thực tế, bạn sẽ dùng component <Link>
    <a href={`/events/${id}`} className="block">
      <div className="bg-card rounded-lg overflow-hidden hover:bg-accent/50 transition-colors group border">
        <div className="relative aspect-[4/3]">
          <img
            src={image || `https://placehold.co/600x400/222/fff?text=${encodeURIComponent(title)}`}
            alt={title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <div className="absolute top-2 left-2">
            <span className="bg-blue-500 text-white px-2 py-1 rounded text-xs font-medium">{categoryName}</span>
          </div>
        </div>
        <div className="p-4 space-y-2">
          <h3 className="font-semibold text-card-foreground line-clamp-2 group-hover:text-blue-600 transition-colors h-14">
            {title}
          </h3>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <Calendar className="w-4 h-4" />
            <span>{formatDate(startTime)}</span>
          </div>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <MapPin className="w-4 h-4" />
            <span className="line-clamp-1">{venue}</span>
          </div>
          <div className="text-green-500 font-semibold text-lg">Từ {formatPrice(price)}</div>
        </div>
      </div>
    </a>
  );
}
