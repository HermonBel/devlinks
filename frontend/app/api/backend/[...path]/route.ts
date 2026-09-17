import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const COOKIE_NAME = 'auth_token';

async function handler(
    req: NextRequest,
    { params }: { params: Promise<{ path: string[] }> }
) {
    const { path } = await params;
    const cookieStore = await cookies();
    const token = cookieStore.get(COOKIE_NAME)?.value;

    const url = new URL(req.url);
    const targetUrl = `${API_URL}/api/${path.join('/')}${url.search}`;

    const headers: HeadersInit = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const init: RequestInit = {
        method: req.method,
        headers,
    };

    if (req.method !== 'GET' && req.method !== 'HEAD') {
        init.body = await req.text();
    }

    const backendRes = await fetch(targetUrl, init);
    const contentType = backendRes.headers.get('Content-Type') || 'application/json';

    // 204 No Content and 304 Not Modified cannot have a body
    if (backendRes.status === 204 || backendRes.status === 304) {
        return new NextResponse(null, {
            status: backendRes.status,
            headers: { 'Content-Type': contentType },
        });
    }

    const data = await backendRes.text();

    return new NextResponse(data, {
        status: backendRes.status,
        headers: { 'Content-Type': contentType },
    });
}

export {
    handler as GET,
    handler as POST,
    handler as PUT,
    handler as DELETE,
    handler as PATCH,
};