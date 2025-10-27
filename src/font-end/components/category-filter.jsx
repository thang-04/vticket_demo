"use client";
import { useState, useEffect } from "react";
import { ChevronDown, Filter } from "lucide-react";

// --- Mock Components for standalone example ---
const Button = ({ children, className, ...props }) => (
  <button
    {...props}
    className={`inline-flex items-center justify-center rounded-md text-sm font-medium ${className}`}
  >
    {children}
  </button>
);

const Checkbox = ({ id, checked, onCheckedChange }) => (
    <input type="checkbox" id={id} checked={checked} onChange={onCheckedChange} className="mr-2" />
);
// --- End Mock Components ---


export function CategoryFilter() {
  const [apiCategories, setApiCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [expandedCategories, setExpandedCategories] = useState(["chuyen-muc"]);
  const [selectedFilters, setSelectedFilters] = useState([]);
  
  // Gọi API để lấy danh sách categories
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        const response = await fetch("http://localhost:8080/vticket/event/categories");
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        if (data && data.code === 1000 && Array.isArray(data.result)) {
          setApiCategories(data.result);
        } else {
          throw new Error("Invalid data structure from API");
        }
      } catch (e) {
        setError(e.message);
        console.error("Failed to fetch categories:", e);
      } finally {
        setLoading(false);
      }
    };
    fetchCategories();
  }, []);

  // Tạo danh sách bộ lọc cuối cùng bằng cách kết hợp dữ liệu từ API và dữ liệu cứng
  const filterSections = [
    {
        id: "chuyen-muc",
        label: "Chuyên mục",
        items: loading ? [] : apiCategories.map(cat => cat.name), // Lấy tên từ API
    },
    // Các mục lọc khác giữ nguyên
    { id: "vi-tri", label: "Vị trí", items: ["Toàn quốc", "Hà Nội", "Hà Giang", "Cao Bằng", "Bắc Kạn", "Tuyên Quang"] },
    {
      id: "muc-gia",
      label: "Mức giá",
      items: [
        "Tất cả",
        "Miễn phí",
        "Dưới 500 nghìn đồng",
        "Từ 1 - 3 triệu đồng",
        "Từ 3 - 7 triệu đồng",
        "Trên 7 triệu đồng",
      ],
    },
  ];


  const toggleCategory = (categoryId) => {
    setExpandedCategories((prev) =>
      prev.includes(categoryId) ? prev.filter((id) => id !== categoryId) : [...prev, categoryId],
    );
  };

  const toggleFilter = (filter) => {
    setSelectedFilters((prev) => (prev.includes(filter) ? prev.filter((f) => f !== filter) : [...prev, filter]));
  };

  return (
    <aside className="w-80 bg-card rounded-lg p-6 h-fit border shrink-0">
      <div className="flex items-center gap-2 mb-6">
        <Filter className="w-5 h-5 text-foreground" />
        <h2 className="text-lg font-semibold text-card-foreground">Bộ lọc tìm kiếm</h2>
      </div>

      <div className="space-y-4">
        {error && <p className="text-red-500 text-sm">Lỗi tải bộ lọc: {error}</p>}
        {filterSections.map((category) => (
          <div key={category.id} className="border-b border-border pb-4 last:border-b-0">
            <Button
              variant="ghost"
              className="w-full justify-between p-0 h-auto text-card-foreground hover:bg-transparent"
              onClick={() => toggleCategory(category.id)}
            >
              <span className="font-medium">{category.label}</span>
              <ChevronDown
                className={`w-4 h-4 transition-transform ${
                  expandedCategories.includes(category.id) ? "rotate-180" : ""
                }`}
              />
            </Button>

            {expandedCategories.includes(category.id) && (
              <div className="mt-3 space-y-2">
                 {category.id === 'chuyen-muc' && loading && <p className="text-sm text-muted-foreground">Đang tải...</p>}
                {category.items.map((item) => (
                  <div key={item} className="flex items-center space-x-2">
                    <Checkbox
                      id={`${category.id}-${item}`}
                      checked={selectedFilters.includes(item)}
                      onCheckedChange={() => toggleFilter(item)}
                    />
                    <label
                      htmlFor={`${category.id}-${item}`}
                      className="text-sm text-muted-foreground cursor-pointer hover:text-foreground"
                    >
                      {item}
                    </label>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </aside>
  );
}
