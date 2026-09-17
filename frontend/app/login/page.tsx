'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

type FieldErrors = Record<string, string>;

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [globalError, setGlobalError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setFieldErrors({});
        setGlobalError(null);
        setSubmitting(true);

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            if (res.ok) {
                router.push('/');
                router.refresh();
                return;
            }

            const errorBody = await res.json().catch(() => null);
            if (errorBody?.fields) setFieldErrors(errorBody.fields);
            else if (errorBody?.message) setGlobalError(errorBody.message);
            else setGlobalError('Login failed');
        } catch {
            setGlobalError('Network error. Is the backend running?');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <main className="p-8 max-w-md mx-auto">
            <h1 className="text-2xl font-bold mb-4">Log In</h1>

            {globalError && (
                <div className="bg-red-50 border border-red-300 text-red-700 p-3 rounded mb-4">
                    {globalError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Email"
                        className="border p-2 w-full rounded"
                    />
                    {fieldErrors.email && (
                        <p className="text-red-600 text-sm mt-1">{fieldErrors.email}</p>
                    )}
                </div>

                <div>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Password"
                        className="border p-2 w-full rounded"
                    />
                    {fieldErrors.password && (
                        <p className="text-red-600 text-sm mt-1">{fieldErrors.password}</p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={submitting}
                    className="bg-blue-600 text-white px-4 py-2 rounded w-full disabled:opacity-50"
                >
                    {submitting ? 'Logging in...' : 'Log In'}
                </button>
            </form>

            <p className="text-sm text-gray-600 mt-4">
                No account?{' '}
                <Link href="/register" className="text-blue-600 underline">
                    Register here
                </Link>
            </p>
        </main>
    );
}