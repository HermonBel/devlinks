import Link from 'next/link';

export default function Pagination({
                                       currentPage,
                                       totalPages,
                                       q,
                                       tag,
                                       sortBy,
                                       direction,
                                   }: {
    currentPage: number;
    totalPages: number;
    q?: string;
    tag?: string;
    sortBy: string;
    direction: string;
}) {
    if (totalPages <= 1) return null;

    const buildHref = (page: number) => {
        const params = new URLSearchParams();
        if (q) params.set('q', q);
        if (tag) params.set('tag', tag);
        params.set('sortBy', sortBy);
        params.set('direction', direction);
        params.set('page', String(page));
        return `/?${params.toString()}`;
    };

    const hasPrev = currentPage > 0;
    const hasNext = currentPage < totalPages - 1;

    return (
        <div className="flex items-center justify-center gap-3 mt-6">
            {hasPrev ? (
                <Link href={buildHref(currentPage - 1)} className="px-3 py-1 border rounded hover:bg-gray-100">
                    ← Prev
                </Link>
            ) : (
                <span className="px-3 py-1 border rounded opacity-40">← Prev</span>
            )}

            <span className="text-sm text-gray-700">
        Page {currentPage + 1} of {totalPages}
      </span>

            {hasNext ? (
                <Link href={buildHref(currentPage + 1)} className="px-3 py-1 border rounded hover:bg-gray-100">
                    Next →
                </Link>
            ) : (
                <span className="px-3 py-1 border rounded opacity-40">Next →</span>
            )}
        </div>
    );
}