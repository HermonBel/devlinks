'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

type FieldErrors = Record<string, string>;

export default function NewBookmark() {
    const router = useRouter();
    const [title, setTitle] = useState('');
    const [url, setUrl] = useState('');
    const [description, setDescription] = useState('');
    const [tags, setTags] = useState<string[]>([]);
    const [tagInput, setTagInput] = useState('');
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [globalError, setGlobalError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const addTag = () => {
        const t = tagInput.trim().toLowerCase();
        if (t && !tags.includes(t)) {
            setTags([...tags, t]);
        }
        setTagInput('');
    };

    const handleTagKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault();
            addTag();
        } else if (e.key === 'Backspace' && !tagInput && tags.length > 0) {
            setTags(tags.slice(0, -1));
        }
    };

    const removeTag = (tag: string) => {
        setTags(tags.filter((t) => t !== tag));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setFieldErrors({});
        setGlobalError(null);
        setSubmitting(true);

        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/bookmarks`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    title,
                    url,
                    description,
                    tags: tags.map((name) => ({ name })),
                }),
            });

            if (res.ok) {
                router.push('/');
                router.refresh();
                return;
            }

            const errorBody = await res.json().catch(() => null);
            if (errorBody?.fields) setFieldErrors(errorBody.fields);
            else if (errorBody?.message) setGlobalError(errorBody.message);
            else setGlobalError('Something went wrong.');
        } catch {
            setGlobalError('Network error. Is the backend running?');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <main className="p-8 max-w-md">
            <h1 className="text-2xl font-bold mb-4">Add Bookmark</h1>

            {globalError && (
                <div className="bg-red-50 border border-red-300 text-red-700 p-3 rounded mb-4">
                    {globalError}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <input
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="Title"
                        className={`border p-2 w-full rounded ${
                            fieldErrors.title ? 'border-red-500' : ''
                        }`}
                    />
                    {fieldErrors.title && (
                        <p className="text-red-600 text-sm mt-1">{fieldErrors.title}</p>
                    )}
                </div>

                <div>
                    <input
                        value={url}
                        onChange={(e) => setUrl(e.target.value)}
                        placeholder="URL"
                        className={`border p-2 w-full rounded ${
                            fieldErrors.url ? 'border-red-500' : ''
                        }`}
                    />
                    {fieldErrors.url && (
                        <p className="text-red-600 text-sm mt-1">{fieldErrors.url}</p>
                    )}
                </div>

                <div>
                    <input
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Description (optional)"
                        className="border p-2 w-full rounded"
                    />
                </div>

                {/* Tag input */}
                <div>
                    <div className="flex flex-wrap gap-2 mb-2">
                        {tags.map((tag) => (
                            <span
                                key={tag}
                                className="bg-blue-100 text-blue-800 text-sm px-2 py-1 rounded flex items-center gap-1"
                            >
                #{tag}
                                <button
                                    type="button"
                                    onClick={() => removeTag(tag)}
                                    className="text-blue-800 hover:text-red-600 font-bold"
                                >
                  ×
                </button>
              </span>
                        ))}
                    </div>
                    <input
                        value={tagInput}
                        onChange={(e) => setTagInput(e.target.value)}
                        onKeyDown={handleTagKeyDown}
                        onBlur={addTag}
                        placeholder="Type a tag and press Enter"
                        className="border p-2 w-full rounded"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                        Press Enter or comma to add. Backspace to remove the last tag.
                    </p>
                </div>

                <button
                    type="submit"
                    disabled={submitting}
                    className="bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {submitting ? 'Saving...' : 'Save'}
                </button>
            </form>
        </main>
    );
}