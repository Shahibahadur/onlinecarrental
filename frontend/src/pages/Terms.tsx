import React from 'react';

const Terms: React.FC = () => {
  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Terms & Conditions</h1>
          <p className="text-xl text-primary-100 max-w-3xl">
            Please read our terms and conditions carefully before using our service
          </p>
        </div>
      </section>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="bg-white rounded-lg shadow-sm p-8 space-y-8">
          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">1. Rental Agreement</h2>
            <p className="text-neutral-600 leading-relaxed mb-4">
              By booking a vehicle through DriveRental Nepal, you agree to these terms and conditions. 
              The rental agreement becomes effective upon confirmation of your booking and payment.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">2. Eligibility</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Minimum age: 21 years for self-drive rentals</li>
              <li>Valid driving license (Nepal or International Driving Permit)</li>
              <li>Valid National ID or Passport</li>
              <li>Credit/debit card for security deposit (if applicable)</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">3. Booking & Payment</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Booking confirmation is subject to vehicle availability</li>
              <li>Payment can be made via cash, eSewa, Khalti, bank transfer, or credit/debit card</li>
              <li>Security deposit may be required and will be refunded upon vehicle return in good condition</li>
              <li>All prices are in Nepalese Rupees (NPR) unless stated otherwise</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">4. Cancellation Policy</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Free cancellation up to 24 hours before scheduled pickup time</li>
              <li>Cancellation within 24 hours: 20% cancellation fee</li>
              <li>No-show: Full booking amount charged</li>
              <li>Full refund for cancellations due to weather emergencies or force majeure</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">5. Vehicle Use & Restrictions</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Vehicle must be used only within Nepal borders</li>
              <li>Driving must comply with all Nepal traffic laws and regulations</li>
              <li>No smoking, drinking, or illegal activities in the vehicle</li>
              <li>Vehicle must not be used for commercial purposes without prior written consent</li>
              <li>Seat belts must be worn at all times</li>
              <li>Maximum number of passengers as per vehicle capacity only</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">6. Insurance & Liability</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Basic insurance is included in all rentals covering third-party liability</li>
              <li>Comprehensive insurance is optional (रू 500/day) covering damage, theft, and comprehensive coverage</li>
              <li>Renter is responsible for any damage not covered by insurance</li>
              <li>In case of accident, renter must immediately contact police and our support team</li>
              <li>Renter is liable for traffic fines and violations during rental period</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">7. Fuel Policy</h2>
            <p className="text-neutral-600 leading-relaxed mb-4">
              Vehicle will be provided with a full tank. Renter must return the vehicle with the same fuel level. 
              If returned with less fuel, refueling charges will apply at current market rates.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">8. Damage & Loss</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Renter is responsible for all damage to the vehicle during rental period</li>
              <li>Any damage must be reported immediately</li>
              <li>Loss or damage to accessories, keys, or documents will be charged at replacement cost</li>
              <li>Vehicle must be returned in the same condition as received (normal wear excepted)</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">9. Extension & Late Return</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Extension requests must be made in advance and are subject to availability</li>
              <li>Late return without extension: Additional charges apply (1.5x daily rate)</li>
              <li>Vehicle not returned within 48 hours of due date may be reported as stolen</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">10. Driver Service</h2>
            <ul className="list-disc list-inside text-neutral-600 space-y-2">
              <li>Professional drivers are licensed and experienced</li>
              <li>Driver service includes driver salary, food, and accommodation</li>
              <li>Driver working hours: 8 AM to 8 PM. Overtime charges apply for extended hours</li>
              <li>Renter is responsible for driver's accommodation during overnight trips</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">11. Limitation of Liability</h2>
            <p className="text-neutral-600 leading-relaxed mb-4">
              DriveRental Nepal shall not be liable for any indirect, incidental, or consequential damages 
              including loss of profits, delays, or inconvenience arising from vehicle breakdown, accidents, 
              or other circumstances beyond our control.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">12. Dispute Resolution</h2>
            <p className="text-neutral-600 leading-relaxed mb-4">
              Any disputes shall be resolved through mutual negotiation. If unresolved, disputes will be 
              subject to the jurisdiction of courts in Kathmandu, Nepal.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">13. Changes to Terms</h2>
            <p className="text-neutral-600 leading-relaxed mb-4">
              DriveRental Nepal reserves the right to modify these terms at any time. Changes will be 
              effective immediately upon posting on our website. Continued use of our service constitutes 
              acceptance of modified terms.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-neutral-900 mb-4">14. Contact</h2>
            <p className="text-neutral-600 leading-relaxed">
              For questions about these terms, please contact us at{' '}
              <a href="mailto:info@driverental.com.np" className="text-primary-600 hover:underline">
                info@driverental.com.np
              </a>
              {' '}or call{' '}
              <a href="tel:+9779800000000" className="text-primary-600 hover:underline">
                +977-9800000000
              </a>
            </p>
          </section>

          <div className="bg-neutral-50 p-6 rounded-lg mt-8">
            <p className="text-sm text-neutral-600">
              <strong>Last Updated:</strong> {new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Terms;
