import Link from 'next/link';
import { fetchTags } from '@/lib/api';

export default async function TagFilter({ activeTag }: { activeTag?: string }) {
    const tags = await fetchTags();
    if (tags.length === 0) return null;

    const sorted = [...tags].sort((a, b) => a.name.localeCompare(b.name));

    return (
        <div className="flex flex-wrap gap-2 mb-6">
            {activeTag && (
                <Link
                    href="/"
                    className="text-sm px-3 py-1 rounded border border-gray-400 hover:bg-gray-100"
                >
                    Clear filter
                </Link>
            )}
            {sorted.map((tag) => (
                <Link
                    key={tag.id}
                    href={`/?tag=${encodeURIComponent(tag.name)}`}
                    className={`text-sm px-3 py-1 rounded border ${
                        tag.name === activeTag
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'border-gray-300 hover:bg-gray-100'
                    }`}
                >
                    #{tag.name}
                </Link>
            ))}
        </div>
    );
}