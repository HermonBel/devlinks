import { NextRequest, NextResponse } from 'next/server';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const COOKIE_NAME = 'auth_token';
const ONE_HOUR = 60 * 60;

export async function POST(req: NextRequest) {
    const rawBody = await req.text();
    console.log('=== [1] RAW BODY FROM BROWSER ===');
    console.log(JSON.stringify(rawBody));

    let body;
    try {
        body = JSON.parse(rawBody);
    } catch (e) {
        console.error('=== [2] PARSE FAILED ===');
        console.error(e);
        return NextResponse.json({ error: 'Invalid JSON from client', rawBody }, { status: 400 });
    }

    const forwardedBody = JSON.stringify(body);
    console.log('=== [3] FORWARDING TO SPRING ===');
    console.log(forwardedBody);

    const backendRes = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: forwardedBody,
    });

    const data = await backendRes.json();

    if (!backendRes.ok) {
        return NextResponse.json(data, { status: backendRes.status });
    }

    const response = NextResponse.json({ user: data.user }, { status: 200 });

    response.cookies.set({
        name: COOKIE_NAME,
        value: data.token,
        httpOnly: true,
        sameSite: 'strict',
        secure: process.env.NODE_ENV === 'production',
        path: '/',
        maxAge: ONE_HOUR,
    });

    return response;
}