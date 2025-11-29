import React from 'react';
import { Link } from 'react-router-dom';
import { Car, Facebook, Instagram } from 'lucide-react';
import { CONTENT, LOCALE } from '../../constants/locale';

const Footer: React.FC = () => {
  return (
    <footer className="bg-neutral-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div className="col-span-1 md:col-span-2">
            <Link to="/" className="flex items-center space-x-2 mb-4">
              <Car className="h-8 w-8 text-primary-400" />
              <span className="text-xl font-bold">DriveRental Nepal</span>
            </Link>
            <p className="text-neutral-400 mb-4 max-w-md">
              {CONTENT.footer.description}
            </p>
            <div className="flex space-x-4">
              <a href={LOCALE.socialMedia.facebook} target="_blank" rel="noopener noreferrer" className="text-neutral-400 hover:text-white transition-colors">
                <Facebook className="h-6 w-6" />
              </a>
              <a href={LOCALE.socialMedia.instagram} target="_blank" rel="noopener noreferrer" className="text-neutral-400 hover:text-white transition-colors">
                <Instagram className="h-6 w-6" />
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2">
              {CONTENT.footer.quickLinks.map((link) => (
                <li key={link.href}>
                  <Link to={link.href} className="text-neutral-400 hover:text-white transition-colors">
                    {link.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Contact</h3>
            <ul className="space-y-2 text-neutral-400">
              <li>{LOCALE.contact.address}</li>
              <li>{LOCALE.contact.email}</li>
              <li>{LOCALE.contact.phone}</li>
              <li className="text-sm mt-3">{LOCALE.contact.workingHours}</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-neutral-800 mt-8 pt-8 text-center text-neutral-400">
          <p>&copy; {new Date().getFullYear()} DriveRental Nepal. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;