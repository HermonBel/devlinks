'use client';

import { useRouter, useSearchParams } from 'next/navigation';

const options = [
    { label: 'Newest first', sortBy: 'id', direction: 'desc' },
    { label: 'Oldest first', sortBy: 'id', direction: 'asc' },
    { label: 'Title A → Z', sortBy: 'title', direction: 'asc' },
    { label: 'Title Z → A', sortBy: 'title', direction: 'desc' },
] as const;

export default function SortDropdown({
                                         sortBy,
                                         direction,
                                     }: {
    sortBy: string;
    direction: string;
}) {
    const router = useRouter();
    const searchParams = useSearchParams();

    const current = `${sortBy},${direction}`;

    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const [newSortBy, newDirection] = e.target.value.split(',');
        const params = new URLSearchParams(searchParams.toString());
        params.set('sortBy', newSortBy);
        params.set('direction', newDirection);
        params.delete('page');
        router.push(`/?${params.toString()}`);
    };

    return (
        <select
            value={current}
            onChange={handleChange}
            className="border p-2 rounded text-sm"
        >
            {options.map((o) => (
                <option key={`${o.sortBy},${o.direction}`} value={`${o.sortBy},${o.direction}`}>
                    {o.label}
                </option>
            ))}
        </select>
    );
}