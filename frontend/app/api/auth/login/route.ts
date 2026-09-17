import { NextRequest, NextResponse } from 'next/server';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const COOKIE_NAME = 'auth_token';
const ONE_HOUR = 60 * 60; // seconds

export async function POST(req: NextRequest) {
    const body = await req.json();

    const backendRes = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });

    const data = await backendRes.json();

    if (!backendRes.ok) {
        return NextResponse.json(data, { status: backendRes.status });
    }

    // Set httpOnly cookie with the JWT
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