"use client";
import React, { useState, useEffect } from "react";
import { ChevronDown } from "lucide-react";
import { EventCard } from "@/components/event-card";
import { CategoryFilter } from "@/components/category-filter";

// Định nghĩa kiểu dữ liệu cho dữ liệu Event từ API
interface Category {
  id: number;
  name: string;
}

interface EventData {
  event_id: number;
  title: string;
  start_time: string;
  venue: string;
  price: number;
  category: Category;
}


// Components giả lập (để ví dụ có thể chạy được)
const Header: React.FC = () => (
  <header className="bg-card border-b p-4">
    <h1 className="text-2xl font-bold">V-Ticket</h1>
  </header>
);

const Button: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>> = ({ children, ...props }) => (
  <button
    {...props}
    className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2"
  >
    {children}
  </button>
);

export default function EventsPage() {
  const [events, setEvents] = useState<EventData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const response = await fetch("http://localhost:8080/vticket/event/list");
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        if (data && data.code === 1000 && Array.isArray(data.result)) {
          setEvents(data.result);
        } else {
          throw new Error("Invalid data structure from API");
        }
      } catch (e: any) {
        console.error("Failed to fetch events:", e);
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  return (
    <div className="min-h-screen bg-background text-foreground">
      <Header />
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-6">
          <span>Trang chủ</span>
          <span>{">"}</span>
          <span className="text-foreground">Khám phá dịch vụ</span>
        </div>
        <div className="flex flex-col lg:flex-row gap-6">
          {/* Component CategoryFilter thật được sử dụng ở đây */}
          <CategoryFilter />
          <div className="flex-1">
            <div className="flex items-center justify-between mb-6">
              <h1 className="text-xl font-semibold text-foreground">Kết quả tìm kiếm:</h1>
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">Sắp xếp theo:</span>
                <Button>
                  Mới nhất
                  <ChevronDown className="w-4 h-4 ml-2" />
                </Button>
              </div>
            </div>

            {loading && <p>Đang tải sự kiện...</p>}
            {error && <p className="text-red-500">Lỗi khi tải sự kiện: {error}</p>}

            {!loading && !error && (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {events.map((event) => (
                    <EventCard
                      key={event.event_id}
                      id={event.event_id}
                      title={event.title}
                      startTime={event.start_time}
                      venue={event.venue}
                      price={event.price}
                      image={null}
                      categoryName={event.category.name}
                    />
                  ))}
                </div>
                <div className="flex justify-center mt-8">
                  <Button>Xem thêm sự kiện</Button>
                </div>
              </>
            )}
            {!loading && !error && events.length === 0 && (
              <p>Không tìm thấy sự kiện nào.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

