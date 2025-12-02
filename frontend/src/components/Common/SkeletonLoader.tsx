import React from 'react';

interface SkeletonLoaderProps {
  count?: number;
  height?: string;
}

const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({ count = 3, height = 'h-12' }) => {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={i}
          className={`${height} bg-neutral-200 rounded-lg animate-pulse`}
        />
      ))}
    </div>
  );
};

export default SkeletonLoader;
