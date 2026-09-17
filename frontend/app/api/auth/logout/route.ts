import { NextResponse } from 'next/server';

const COOKIE_NAME = 'auth_token';

export async function POST() {
    const response = NextResponse.json({ ok: true });
    response.cookies.set({
        name: COOKIE_NAME,
        value: '',
        httpOnly: true,
        sameSite: 'strict',
        secure: process.env.NODE_ENV === 'production',
        path: '/',
        maxAge: 0,   // delete cookie
    });
    return response;
}