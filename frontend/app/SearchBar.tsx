'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

export default function SearchBar({ initialValue }: { initialValue: string }) {
    const router = useRouter();
    const searchParams = useSearchParams();
    const [value, setValue] = useState(initialValue);

    useEffect(() => {
        // Don't push if we're already in sync with the URL
        if (value === initialValue) return;

        const timer = setTimeout(() => {
            const params = new URLSearchParams(searchParams.toString());
            if (value) params.set('q', value);
            else params.delete('q');
            params.delete('page');
            router.push(`/?${params.toString()}`);
        }, 400);

        return () => clearTimeout(timer);
    }, [value, initialValue, router, searchParams]);

    return (
        <input
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder="Search title or description..."
            className="border p-2 rounded text-sm w-64"
        />
    );
}