import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const COOKIE_NAME = 'auth_token';

export async function GET(req: NextRequest) {
    const cookieStore = await cookies();
    const token = cookieStore.get(COOKIE_NAME)?.value;

    if (!token) {
        return NextResponse.json({ status: 401, error: 'Unauthorized' }, { status: 401 });
    }

    const backendRes = await fetch(`${API_URL}/api/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
    });

    if (!backendRes.ok) {
        return NextResponse.json({ status: 401, error: 'Unauthorized' }, { status: 401 });
    }

    return NextResponse.json(await backendRes.json());
}