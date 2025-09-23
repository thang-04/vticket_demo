"use client"
import { useState } from "react"
import { ChevronDown, Filter } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"

const categories = [
  { id: "chuyen-muc", label: "Chuyên mục", items: ["Âm nhạc", "Giải trí", "Thể thao", "Giáo dục"] },
  { id: "am-nhac", label: "Âm nhạc", items: ["Pop", "Rock", "Classical", "Jazz"] },
  { id: "giai-tri", label: "Giải trí", items: ["Concert", "Show", "Festival", "Theater"] },
  { id: "the-thao", label: "Thể thao", items: ["Bóng đá", "Tennis", "Basketball", "Volleyball"] },
  { id: "giao-duc", label: "Giáo dục", items: ["Workshop", "Seminar", "Conference", "Training"] },
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
]

export function CategoryFilter() {
  const [expandedCategories, setExpandedCategories] = useState<string[]>(["chuyen-muc"])
  const [selectedFilters, setSelectedFilters] = useState<string[]>([])

  const toggleCategory = (categoryId: string) => {
    setExpandedCategories((prev) =>
      prev.includes(categoryId) ? prev.filter((id) => id !== categoryId) : [...prev, categoryId],
    )
  }

  const toggleFilter = (filter: string) => {
    setSelectedFilters((prev) => (prev.includes(filter) ? prev.filter((f) => f !== filter) : [...prev, filter]))
  }

  return (
    <div className="w-80 bg-card rounded-lg p-6 h-fit">
      <div className="flex items-center gap-2 mb-6">
        <Filter className="w-5 h-5 text-foreground" />
        <h2 className="text-lg font-semibold text-card-foreground">Bộ lọc tìm kiếm</h2>
      </div>

      <div className="space-y-4">
        {categories.map((category) => (
          <div key={category.id} className="border-b border-border pb-4 last:border-b-0">
            <Button
              variant="ghost"
              className="w-full justify-between p-0 h-auto text-card-foreground hover:text-primary"
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
    </div>
  )
}
