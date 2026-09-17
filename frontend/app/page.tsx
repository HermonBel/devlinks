import Link from 'next/link';
import { redirect } from 'next/navigation';
import BookmarkActions from './BookmarkActions';
import SearchBar from './SearchBar';
import TagFilter from './TagFilter';
import Pagination from './Pagination';
import SortDropdown from './SortDropdown';
import UserMenu from './UserMenu';
import { fetchBookmarks, fetchCurrentUser } from '@/lib/api';

export default async function Home({
                                       searchParams,
                                   }: {
    searchParams: Promise<{
        q?: string;
        tag?: string;
        page?: string;
        sortBy?: string;
        direction?: 'asc' | 'desc';
    }>;
}) {
    const user = await fetchCurrentUser();
    if (!user) {
        redirect('/login');
    }

    const sp = await searchParams;
    const page = Number(sp.page ?? 0);
    const q = sp.q;
    const tag = sp.tag;
    const sortBy = sp.sortBy ?? 'id';
    const direction = sp.direction ?? 'desc';

    const data = await fetchBookmarks({
        q,
        tag,
        page,
        size: 5,
        sortBy,
        direction,
    });

    const hasFilters = Boolean(q || tag);

    return (
        <main className="p-8 max-w-3xl mx-auto">
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-2xl font-bold">DevLinks</h1>
                <UserMenu user={user} />
            </div>

            <div className="flex flex-wrap gap-3 mb-4 items-center">
                <Link
                    href="/bookmarks/new"
                    className="bg-blue-600 text-white px-3 py-2 rounded text-sm"
                >
                    + Add Bookmark
                </Link>
                <SearchBar initialValue={q ?? ''} />
                <SortDropdown sortBy={sortBy} direction={direction} />
            </div>

            <TagFilter activeTag={tag} />

            {hasFilters && (
                <p className="text-sm text-gray-600 mb-3">
                    {data.page.totalElements} result{data.page.totalElements === 1 ? '' : 's'}
                    {q && <> for &ldquo;<strong>{q}</strong>&rdquo;</>}
                    {tag && <> tagged <strong>#{tag}</strong></>}
                    {' · '}
                    <Link href="/" className="text-blue-600 underline">
                        Clear
                    </Link>
                </p>
            )}

            {data.content.length === 0 ? (
                <p className="text-gray-500">No bookmarks found.</p>
            ) : (
                <ul className="space-y-3">
                    {data.content.map((bm) => (
                        <li key={bm.id} className="border p-4 rounded">
                            <a
                                href={bm.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="font-semibold text-blue-600"
                            >
                                {bm.title}
                            </a>
                            <p className="text-gray-600 text-sm">{bm.description}</p>

                            <div className="flex flex-wrap gap-2 mt-2">
                                {bm.tags?.map((t) => (
                                    <Link
                                        key={t.id}
                                        href={`/?tag=${encodeURIComponent(t.name)}`}
                                        className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded hover:bg-blue-200"
                                    >
                                        #{t.name}
                                    </Link>
                                ))}
                            </div>

                            <BookmarkActions id={bm.id} />
                        </li>
                    ))}
                </ul>
            )}

            <Pagination
                currentPage={data.page.number}
                totalPages={data.page.totalPages}
                q={q}
                tag={tag}
                sortBy={sortBy}
                direction={direction}
            />
        </main>
    );
}