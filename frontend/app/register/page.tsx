'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

type FieldErrors = Record<string, string>;

export default function RegisterPage() {
    const router = useRouter();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [displayName, setDisplayName] = useState('');
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [globalError, setGlobalError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setFieldErrors({});
        setGlobalError(null);
        setSubmitting(true);

        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password, displayName }),
            });

            if (res.ok) {
                router.push('/');
                router.refresh();
                return;
            }

            const errorBody = await res.json().catch(() => null);
            if (errorBody?.fields) setFieldErrors(errorBody.fields);
            else if (errorBody?.message) setGlobalError(errorBody.message);
            else setGlobalError('Registration failed');
        } catch {
            setGlobalError('Network error. Is the backend running?');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <main className="p-8 max-w-md mx-auto">
            <h1 className="text-2xl font-bold mb-4">Register</h1>

            {globalError && (
                <div className="bg-red-50 border border-red-300 text-red-700 p-3 rounded mb-4">
                    {globalError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <input
                        type="text"
                        value={displayName}
                        onChange={(e) => setDisplayName(e.target.value)}
                        placeholder="Display name"
                        className="border p-2 w-full rounded"
                    />
                    {fieldErrors.displayName && (
                        <p className="text-red-600 text-sm mt-1">{fieldErrors.displayName}</p>
                    )}
                </div>

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
                        placeholder="Password (min 8 characters)"
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
                    {submitting ? 'Registering...' : 'Register'}
                </button>
            </form>

            <p className="text-sm text-gray-600 mt-4">
                Already have an account?{' '}
                <Link href="/login" className="text-blue-600 underline">
                    Log in
                </Link>
            </p>
        </main>
    );
}