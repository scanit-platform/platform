"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createCustomCategory,
  deleteCustomCategory,
  updateCustomCategory,
} from "@/src/entities/category/api/categories-service";
import { categoryQueryKeys } from "@/src/entities/category/model/category-query-keys";
import type {
  CreateCustomCategoryInput,
  UpdateCustomCategoryInput,
} from "@/src/entities/category/types/category";

function useInvalidateCategoryQueries() {
  const queryClient = useQueryClient();

  return () =>
    queryClient.invalidateQueries({
      queryKey: categoryQueryKeys.all,
    });
}

export function useCreateCustomCategoryMutation() {
  const invalidateCategoryQueries = useInvalidateCategoryQueries();

  return useMutation({
    mutationFn: (input: CreateCustomCategoryInput) =>
      createCustomCategory(input),
    onSuccess: invalidateCategoryQueries,
  });
}

export function useUpdateCustomCategoryMutation() {
  const invalidateCategoryQueries = useInvalidateCategoryQueries();

  return useMutation({
    mutationFn: (input: UpdateCustomCategoryInput) =>
      updateCustomCategory(input),
    onSuccess: invalidateCategoryQueries,
  });
}

export function useDeleteCustomCategoryMutation() {
  const invalidateCategoryQueries = useInvalidateCategoryQueries();

  return useMutation({
    mutationFn: deleteCustomCategory,
    onSuccess: invalidateCategoryQueries,
  });
}
