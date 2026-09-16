import { notFound } from 'next/navigation';
import EditBookmarkForm from './EditBookmarkForm';

async function getBookmark(id: string) {
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/bookmarks/${id}`,
        { cache: 'no-store' }
    );
    if (res.status === 404) return null;
    if (!res.ok) throw new Error('Failed to fetch bookmark');
    return res.json();
}

export default async function EditBookmarkPage({
                                                   params,
                                               }: {
    params: Promise<{ id: string }>;
}) {
    const { id } = await params;
    const bookmark = await getBookmark(id);
    if (!bookmark) notFound();

    return (
        <main className="p-8 max-w-md">
            <h1 className="text-2xl font-bold mb-4">Edit Bookmark</h1>
            <EditBookmarkForm bookmark={bookmark} />
        </main>
    );
}