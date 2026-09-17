'use client';

import { useRouter } from 'next/navigation';

export default function UserMenu({
                                     user,
                                 }: {
    user: { id: number; email: string; displayName: string };
}) {
    const router = useRouter();

    const handleLogout = async () => {
        await fetch('/api/auth/logout', { method: 'POST' });
        router.push('/login');
        router.refresh();
    };

    return (
        <div className="flex items-center gap-3 text-sm">
            <span className="text-gray-700">Hi, {user.displayName}</span>
            <button
                onClick={handleLogout}
                className="text-red-600 hover:underline"
            >
                Logout
            </button>
        </div>
    );
}