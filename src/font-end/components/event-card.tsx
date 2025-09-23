import Image from "next/image"
import Link from "next/link"
import { Calendar, MapPin } from "lucide-react"

interface EventCardProps {
  id: string
  title: string
  date: string
  location: string
  price: string
  image: string
  category: string
}

export function EventCard({ id, title, date, location, price, image, category }: EventCardProps) {
  return (
    <Link href={`/events/${id}`}>
      <div className="bg-card rounded-lg overflow-hidden hover:bg-accent/50 transition-colors group">
        <div className="relative aspect-[4/3]">
          <Image
            src={image || "/placeholder.svg"}
            alt={title}
            fill
            className="object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <div className="absolute top-2 left-2">
            <span className="bg-primary text-primary-foreground px-2 py-1 rounded text-xs font-medium">{category}</span>
          </div>
        </div>
        <div className="p-4 space-y-2">
          <h3 className="font-semibold text-card-foreground line-clamp-2 group-hover:text-primary transition-colors">
            {title}
          </h3>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <Calendar className="w-4 h-4" />
            <span>{date}</span>
          </div>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <MapPin className="w-4 h-4" />
            <span className="line-clamp-1">{location}</span>
          </div>
          <div className="text-green-500 font-semibold">Từ {price}</div>
        </div>
      </div>
    </Link>
  )
}
