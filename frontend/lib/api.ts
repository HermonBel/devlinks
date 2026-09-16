export type Bookmark = {
    id: number;
    title: string;
    url: string;
    description: string;
    tags: { id: number; name: string }[];
};

export type PagedBookmarks = {
    content: Bookmark[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
};

export type SearchParams = {
    q?: string;
    tag?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: 'asc' | 'desc';
};

export function buildBookmarksUrl(params: SearchParams): string {
    const sp = new URLSearchParams();
    if (params.q) sp.set('q', params.q);
    if (params.tag) sp.set('tag', params.tag);
    if (params.page !== undefined) sp.set('page', String(params.page));
    if (params.size !== undefined) sp.set('size', String(params.size));
    if (params.sortBy) sp.set('sortBy', params.sortBy);
    if (params.direction) sp.set('direction', params.direction);
    return `${process.env.NEXT_PUBLIC_API_URL}/api/bookmarks?${sp.toString()}`;
}

export async function fetchBookmarks(params: SearchParams): Promise<PagedBookmarks> {
    const res = await fetch(buildBookmarksUrl(params), { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch bookmarks');
    return res.json();
}