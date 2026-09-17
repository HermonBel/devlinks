'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';

export default function BookmarkActions({ id }: { id: number }) {
    const router = useRouter();

    const handleDelete = async () => {
        if (!confirm('Delete this bookmark?')) return;

        const res = await fetch(`/api/backend/bookmarks/${id}`, {
            method: 'DELETE',
            credentials: 'include',
        });

        if (res.ok) {
            router.refresh();
        } else {
            alert('Failed to delete');
        }
    };

    return (
        <div className="flex gap-3 text-sm mt-2">
            <Link
                href={`/bookmarks/${id}/edit`}
                className="text-blue-600 hover:underline"
            >
                Edit
            </Link>
            <button onClick={handleDelete} className="text-red-600 hover:underline">
                Delete
            </button>
        </div>
    );
}