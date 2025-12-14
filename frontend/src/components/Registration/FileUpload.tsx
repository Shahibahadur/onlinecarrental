import React, { useRef, useState } from 'react';
import { Upload, X, FileText, CheckCircle2 } from 'lucide-react';

interface FileUploadProps {
  label: string;
  accept?: string;
  required?: boolean;
  value?: File | null;
  onChange: (file: File | null) => void;
  error?: string;
  maxSizeMB?: number;
}

const FileUpload: React.FC<FileUploadProps> = ({
  label,
  accept = 'image/*,.pdf',
  required = false,
  value,
  onChange,
  error,
  maxSizeMB = 5,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragActive, setDragActive] = useState(false);

  const handleFile = (file: File) => {
    if (file.size > maxSizeMB * 1024 * 1024) {
      alert(`File size must be less than ${maxSizeMB}MB`);
      return;
    }
    onChange(file);
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0]);
    }
  };

  const removeFile = () => {
    onChange(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div>
      <label className="block text-sm font-medium text-neutral-700 mb-2">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      
      {!value ? (
        <div
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          className={`
            border-2 border-dashed rounded-lg p-6 text-center cursor-pointer
            transition-colors
            ${dragActive 
              ? 'border-primary-500 bg-primary-50' 
              : 'border-neutral-300 hover:border-primary-400 hover:bg-neutral-50'
            }
            ${error ? 'border-red-300' : ''}
          `}
          onClick={() => fileInputRef.current?.click()}
        >
          <Upload className="h-8 w-8 text-neutral-400 mx-auto mb-2" />
          <p className="text-sm text-neutral-600 mb-1">
            Click to upload or drag and drop
          </p>
          <p className="text-xs text-neutral-500">
            {accept.includes('pdf') ? 'PDF, Images' : 'Images'} (Max {maxSizeMB}MB)
          </p>
          <input
            ref={fileInputRef}
            type="file"
            accept={accept}
            onChange={handleChange}
            className="hidden"
          />
        </div>
      ) : (
        <div className="border border-neutral-300 rounded-lg p-4 bg-neutral-50 flex items-center justify-between">
          <div className="flex items-center space-x-3 flex-1">
            <div className="p-2 bg-primary-100 rounded-lg">
              <FileText className="h-5 w-5 text-primary-600" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-neutral-900 truncate">
                {value.name}
              </p>
              <p className="text-xs text-neutral-500">
                {(value.size / 1024 / 1024).toFixed(2)} MB
              </p>
            </div>
            <CheckCircle2 className="h-5 w-5 text-green-500" />
          </div>
          <button
            type="button"
            onClick={removeFile}
            className="ml-4 p-2 text-neutral-400 hover:text-red-600 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
      )}
      
      {error && (
        <p className="mt-1 text-sm text-red-600">{error}</p>
      )}
    </div>
  );
};

export default FileUpload;

