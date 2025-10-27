"use client";
import React, { useState, useEffect } from "react";
import { ChevronDown, Calendar, MapPin, Ticket } from "lucide-react";

// --- EventCard Component ---
const EventCard = ({ title, startTime, venue, price, categoryName }) => (
  <div className="bg-card border rounded-lg overflow-hidden shadow-lg hover:shadow-2xl transition-shadow duration-300">
    <div className="w-full h-48 bg-gray-200 flex items-center justify-center">
        <span className="text-gray-500">Event Image</span>
    </div>
    <div className="p-4">
      <p className="text-sm font-semibold text-blue-500 mb-1">{categoryName}</p>
      <h3 className="text-lg font-bold truncate">{title}</h3>
      <div className="text-muted-foreground text-sm mt-2 space-y-1">
        <p className="flex items-center"><Calendar className="w-4 h-4 mr-2" />{new Date(startTime).toLocaleString()}</p>
        <p className="flex items-center"><MapPin className="w-4 h-4 mr-2" />{venue}</p>
        <p className="flex items-center font-semibold text-foreground"><Ticket className="w-4 h-4 mr-2" />{price.toLocaleString('vi-VN')} VNĐ</p>
      </div>
    </div>
  </div>
);

// --- Header Component ---
const Header = () => (
  <header className="bg-card border-b p-4">
    <h1 className="text-2xl font-bold">V-Ticket</h1>
  </header>
);

// --- Button Component ---
const Button = ({ children, ...props }) => (
  <button
    {...props}
    className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2"
  >
    {children}
  </button>
);

// --- CategoryFilter Component ---
// This component now receives the categories list as a prop
const CategoryFilter = ({ categories, selectedCategories, onCategoryChange }) => {
    
    const handleCheckboxChange = (categoryId) => {
        const newSelection = selectedCategories.includes(categoryId)
            ? selectedCategories.filter(id => id !== categoryId)
            : [...selectedCategories, categoryId];
        onCategoryChange(newSelection);
    };

    if (!categories.length) {
        return <div className="p-4 border rounded-lg bg-card">Đang tải danh mục...</div>
    }

    return (
        <div className="w-full lg:w-64">
            <h2 className="text-lg font-semibold mb-4">Danh mục</h2>
            <div className="space-y-2">
                {categories.map((category) => (
                    <div key={category.id} className="flex items-center">
                        <input
                            type="checkbox"
                            id={`category-${category.id}`}
                            checked={selectedCategories.includes(category.id)}
                            onChange={() => handleCheckboxChange(category.id)}
                            className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                        />
                        <label htmlFor={`category-${category.id}`} className="ml-2 block text-sm text-foreground">
                            {category.name}
                        </label>
                    </div>
                ))}
            </div>
        </div>
    );
};


//=========== MAIN PAGE COMPONENT (Fixed) ===========

export default function EventsPage() {
  const [events, setEvents] = useState([]);
  const [allCategories, setAllCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategories, setSelectedCategories] = useState([]);

  // Effect to fetch all categories once on component mount
  useEffect(() => {
    const fetchAllCategories = async () => {
        try {
            const response = await fetch("http://localhost:8080/vticket/event/categories");
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
             if (data && data.code === 1000 && Array.isArray(data.result)) {
                const mappedCategories = data.result.map((cat) => ({
                    id: cat.category_id || cat.id || cat.categoryId,
                    name: cat.name || cat.categoryName
                }));
                setAllCategories(mappedCategories);
            } else {
                throw new Error("Invalid data structure for categories");
            }
        } catch (e) {
            console.error("Failed to fetch categories:", e);
            // You might want to set a specific error state for categories here
        }
    };
    fetchAllCategories();
  }, []);

  // Effect to fetch events whenever selectedCategories changes
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        setError(null);
        
        let url = "http://localhost:8080/vticket/event/list";
        
        if (selectedCategories.length > 0) {
          const categoryIds = selectedCategories.join(',');
          url = `http://localhost:8080/vticket/event/search?cateId=${categoryIds}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        if (data && data.code === 1000 && Array.isArray(data.result)) {
          setEvents(data.result);
        } else if (data.code !== 1000) {
           setEvents([]);
           console.warn(`API returned code ${data.code}: ${data.desc}`);
        } else {
          throw new Error("Invalid data structure from API");
        }
      } catch (e) {
        console.error("Failed to fetch events:", e);
        setError(e.message);
        setEvents([]); 
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, [selectedCategories]); 

  const handleCategoryChange = (newCategories) => {
    setSelectedCategories(newCategories);
  };

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
          <CategoryFilter 
            categories={allCategories}
            selectedCategories={selectedCategories}
            onCategoryChange={handleCategoryChange}
          />

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
                  {events.map((event) => {
                    let categoryName = 'N/A'; // Default value

                    if (event.category) {
                        // Case 1: Data from /list endpoint which includes category object
                        categoryName = event.category.name;
                    } else if (selectedCategories.length === 1) {
                        // Case 2: Data from /search endpoint with only ONE category selected
                        const categoryId = selectedCategories[0];
                        const foundCategory = allCategories.find(c => c.id === categoryId);
                        if (foundCategory) {
                            categoryName = foundCategory.name;
                        }
                    }
                  
                    return (
                        <EventCard
                          key={event.event_id}
                          title={event.title}
                          startTime={event.start_time}
                          venue={event.venue}
                          price={event.price}
                          categoryName={categoryName}
                        />
                    );
                  })}
                </div>
                 {events.length === 0 && (
                    <p className="text-center text-muted-foreground mt-8">Không tìm thấy sự kiện nào phù hợp.</p>
                )}
                {events.length > 0 && (
                    <div className="flex justify-center mt-8">
                        <Button>Xem thêm sự kiện</Button>
                    </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
