export type Bookmark = {
    id: number;
    title: string;
    url: string;
    description: string;
    tags: { id: number; name: string }[];
};

export type PagedBookmarks = {
    content: Bookmark[];
    totalElements: number;
    totalPages: number;
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

/**
 * Base URL for server-side fetches.
 * On the server (SSR), we need an absolute URL — relative won't work in Node's fetch.
 * On the client, relative is fine but we use absolute here for consistency.
 */
const SERVER_BASE = process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';

/**
 * Used by Server Components. Reads the cookie from next/headers and forwards it
 * as a Bearer token to Spring Boot. This is what SSR needs.
 */
async function serverFetch(path: string, init?: RequestInit): Promise<Response> {
    const { cookies } = await import('next/headers');
    const cookieStore = await cookies();
    const token = cookieStore.get('auth_token')?.value;

    const url = path.startsWith('http') ? path : `${SERVER_BASE}${path}`;

    const headers: HeadersInit = {
        ...(init?.headers || {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    return fetch(url, { ...init, headers });
}

function buildUrl(
    path: string,
    params?: Record<string, string | number | undefined>
): string {
    const sp = new URLSearchParams();
    if (params) {
        for (const [key, value] of Object.entries(params)) {
            if (value !== undefined && value !== '') sp.set(key, String(value));
        }
    }
    const qs = sp.toString();
    return `${path}${qs ? `?${qs}` : ''}`;
}

/**
 * SERVER-SIDE — call directly to Spring Boot, forwarding the JWT explicitly.
 * Used by Server Components.
 */
export async function fetchBookmarks(
    params: SearchParams
): Promise<PagedBookmarks> {
    const path = buildUrl('/api/bookmarks', {
        q: params.q,
        tag: params.tag,
        page: params.page,
        size: params.size,
        sortBy: params.sortBy,
        direction: params.direction,
    });

    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const res = await serverFetch(`${apiUrl}${path}`, { cache: 'no-store' });

    if (!res.ok) throw new Error('Failed to fetch bookmarks');
    return res.json();
}

export async function fetchTags(): Promise<{ id: number; name: string }[]> {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const res = await serverFetch(`${apiUrl}/api/tags`, { cache: 'no-store' });
    if (!res.ok) return [];
    return res.json();
}

/**
 * Fetch the current user from Spring Boot directly, using the cookie.
 * Runs on the server — this is the "am I logged in?" check.
 */
export async function fetchCurrentUser(): Promise<{
    id: number;
    email: string;
    displayName: string;
} | null> {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const res = await serverFetch(`${apiUrl}/api/auth/me`, { cache: 'no-store' });
    if (!res.ok) return null;
    return res.json();
}