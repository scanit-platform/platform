import { revalidatePath } from "next/cache";
import { apiFetch } from "@/src/shared/api/client";
import { getAuthToken, getAuthenticatedUserOrRedirect } from "@/src/features/auth/model/session";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";
import { PlusIcon } from "@/src/shared/ui/icons/icons";

async function requireAuthToken() {
  const token = await getAuthToken();

  if (!token) {
    throw new Error("Authentication is required.");
  }

  return token;
}

async function createCategoryAction(formData: FormData) {
  "use server";

  const token = await requireAuthToken();
  const generalCategoryId = String(formData.get("generalCategoryId") ?? "");
  const name = String(formData.get("name") ?? "").trim();

  await apiFetch(
    "/api/categories/custom",
    {
      body: JSON.stringify({ generalCategoryId, name }),
      method: "POST",
    },
    { authToken: token },
  );

  revalidatePath("/dashboard/categories");
}

async function renameCategoryAction(formData: FormData) {
  "use server";

  const token = await requireAuthToken();
  const id = String(formData.get("id") ?? "");
  const name = String(formData.get("name") ?? "").trim();

  await apiFetch(
    `/api/categories/custom/${encodeURIComponent(id)}`,
    {
      body: JSON.stringify({ name }),
      method: "PUT",
    },
    { authToken: token },
  );

  revalidatePath("/dashboard/categories");
}

async function deleteCategoryAction(formData: FormData) {
  "use server";

  const token = await requireAuthToken();
  const id = String(formData.get("id") ?? "");

  await apiFetch(
    `/api/categories/custom/${encodeURIComponent(id)}`,
    {
      method: "DELETE",
    },
    { authToken: token },
  );

  revalidatePath("/dashboard/categories");
}

export async function CategoriesView() {
  const user = await getAuthenticatedUserOrRedirect();
  const token = await requireAuthToken();
  const [generalCategories, customCategories] = await Promise.all([
    apiFetch<GeneralCategory[]>("/api/categories/general", {
      method: "GET",
    }),
    apiFetch<CustomCategory[]>(
      "/api/categories/custom",
      {
        method: "GET",
      },
      { authToken: token },
    ).catch(() => []),
  ]);

  return (
    <AppShell activeItem="categories" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6">
            <p className="text-sm font-semibold text-[var(--scanit-primary)]">
              Backend categories
            </p>
            <h1 className="mt-1 font-serif text-[28px] font-bold text-[var(--scanit-text)]">
              Categories
            </h1>
          </div>

          <div className="grid gap-5 lg:grid-cols-[minmax(0,0.7fr)_minmax(0,1.3fr)]">
            <section className="scanit-auth-card p-5">
              <h2 className="font-serif text-xl font-bold text-[var(--scanit-text)]">
                Add Custom Category
              </h2>
              <form action={createCategoryAction} className="mt-5 space-y-4">
                <label className="block">
                  <span className="scanit-form-label">General category</span>
                  <select
                    className="auth-glass-field h-11 w-full rounded-lg px-3 text-sm"
                    name="generalCategoryId"
                    required
                  >
                    {generalCategories.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.icon ? `${category.icon} ` : ""}
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="scanit-form-label">Name</span>
                  <input
                    className="auth-glass-field h-11 w-full rounded-lg px-3 text-sm"
                    maxLength={40}
                    name="name"
                    placeholder="Coffee"
                    required
                  />
                </label>
                <button className="scanit-btn scanit-btn-primary h-11 w-full" type="submit">
                  <PlusIcon />
                  Add Category
                </button>
              </form>
            </section>

            <section className="scanit-auth-card p-5">
              <h2 className="font-serif text-xl font-bold text-[var(--scanit-text)]">
                Custom Categories
              </h2>
              <div className="mt-5 space-y-3">
                {customCategories.length > 0 ? (
                  customCategories.map((category) => (
                    <article
                      className="rounded-lg border border-[var(--scanit-border)] p-4"
                      key={category.id}
                    >
                      <div className="mb-3 flex flex-col justify-between gap-1 sm:flex-row sm:items-center">
                        <div>
                          <h3 className="font-bold text-[var(--scanit-text)]">
                            {category.name}
                          </h3>
                          <p className="text-sm text-[var(--scanit-text-secondary)]">
                            {category.generalCategoryName ??
                              generalCategories.find(
                                (generalCategory) =>
                                  generalCategory.id === category.generalCategoryId,
                              )?.name ??
                              "General category"}
                          </p>
                        </div>
                      </div>
                      <div className="grid gap-2 sm:grid-cols-[minmax(0,1fr)_auto]">
                        <form
                          action={renameCategoryAction}
                          className="flex min-w-0 gap-2"
                        >
                          <input name="id" type="hidden" value={category.id} />
                          <input
                            className="auth-glass-field h-10 min-w-0 flex-1 rounded-lg px-3 text-sm"
                            defaultValue={category.name}
                            maxLength={40}
                            name="name"
                            required
                          />
                          <button
                            className="scanit-btn scanit-btn-secondary h-10"
                            type="submit"
                          >
                            Save
                          </button>
                        </form>
                        <form action={deleteCategoryAction}>
                          <input name="id" type="hidden" value={category.id} />
                          <button
                            className="scanit-btn h-10 border border-[var(--scanit-danger-soft)] bg-[var(--scanit-danger-softer)] text-[var(--scanit-danger-text)]"
                            type="submit"
                          >
                            Delete
                          </button>
                        </form>
                      </div>
                    </article>
                  ))
                ) : (
                  <p className="rounded-lg border border-[var(--scanit-border)] bg-[var(--scanit-soft)] px-4 py-3 text-sm text-[var(--scanit-text-secondary)]">
                    No custom categories yet.
                  </p>
                )}
              </div>
            </section>
          </div>
        </div>
      </section>
    </AppShell>
  );
}
