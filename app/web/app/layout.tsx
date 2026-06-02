import { dmSans, spaceGrotesk } from "@/src/app/fonts";
import { metadata as appMetadata } from "@/src/app/metadata";
import "@/src/app/styles/globals.css";

export const metadata = appMetadata;

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${dmSans.variable} ${spaceGrotesk.variable} h-full antialiased`}
    >
      <body className="min-h-full">{children}</body>
    </html>
  );
}
