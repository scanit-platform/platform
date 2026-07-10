import {
  apiFetch,
  createQueryString,
} from "@/src/shared/api/client";
import type {
  BudgetLimit,
  BudgetLimitInput,
} from "@/src/entities/budget/types/budget";

export type BudgetSearchParams = {
  customCategoryId?: string;
  generalCategoryId?: string;
  period?: string;
  userId: number;
};

type AuthenticatedRequestOptions = {
  authToken?: string;
};

function getAuthOptions(options: AuthenticatedRequestOptions = {}) {
  return options.authToken ? { authToken: options.authToken } : {};
}

export function searchBudgetLimits(
  params: BudgetSearchParams,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<BudgetLimit[]>(
    `/api/budget/search${createQueryString(params)}`,
    {
      method: "GET",
    },
    getAuthOptions(options),
  );
}

export function createBudgetLimit(
  input: BudgetLimitInput,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<BudgetLimit>(
    "/api/budget",
    {
      body: JSON.stringify({
        customCategoryId: input.customCategoryId ?? null,
        generalCategoryId: input.generalCategoryId,
        monthlyLimit: input.monthlyLimit,
        period: input.period,
        userId: input.userId,
      }),
      method: "POST",
    },
    getAuthOptions(options),
  );
}

export function updateBudgetLimit(
  id: number,
  input: BudgetLimitInput,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<BudgetLimit>(
    `/api/budget/${id}`,
    {
      body: JSON.stringify({
        customCategoryId: input.customCategoryId ?? null,
        generalCategoryId: input.generalCategoryId,
        monthlyLimit: input.monthlyLimit,
        period: input.period,
        userId: input.userId,
      }),
      method: "PUT",
    },
    getAuthOptions(options),
  );
}

export function deleteBudgetLimit(
  id: number,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<void>(
    `/api/budget/${id}`,
    {
      method: "DELETE",
    },
    getAuthOptions(options),
  );
}
