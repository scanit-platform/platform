import { DashboardView } from "@/src/views/dashboard/DashboardView";

type DashboardRouteProps = {
  searchParams: Promise<{
    mode?: string | string[] | undefined;
  }>;
};

export default async function DashboardRoute({
  searchParams,
}: DashboardRouteProps) {
  const { mode } = await searchParams;

  return <DashboardView mode={mode} />;
}
