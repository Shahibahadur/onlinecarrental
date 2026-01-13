import React, { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';

const FAQ: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs = [
    {
      question: 'How do I book a car?',
      answer: 'You can book a car directly through our website by selecting your preferred dates, location, and vehicle. Fill in your details and complete the payment. You can also call us or WhatsApp us for assistance with booking.',
    },
    {
      question: 'What documents do I need to rent a car?',
      answer: 'For self-drive: Valid driving license (Nepal or International), National ID/Passport, and a valid credit/debit card. For driver-included rentals, you only need your ID/Passport. Tourist permit assistance is available if needed.',
    },
    {
      question: 'Can I rent a car with a driver?',
      answer: 'Yes! We offer both self-drive and driver-included options. Our professional drivers are experienced, licensed, and familiar with Nepal\'s roads. Driver service costs रू 1,500 per day.',
    },
    {
      question: 'What payment methods do you accept?',
      answer: 'We accept cash, eSewa, Khalti, bank transfers, and credit/debit cards. Payment can be made at the time of booking or upon vehicle pickup.',
    },
    {
      question: 'What is your cancellation policy?',
      answer: 'Free cancellation up to 24 hours before pickup. Cancellations within 24 hours may incur a 20% fee. Full refund for cancellations due to weather or emergencies.',
    },
    {
      question: 'Is insurance included?',
      answer: 'Basic insurance is included in all rentals. Optional comprehensive insurance is available for रू 500 per day, covering damage, theft, and third-party liability.',
    },
    {
      question: 'Can I pick up and drop off at different locations?',
      answer: 'Yes, we offer flexible pickup and drop-off locations across major cities in Nepal. Additional charges may apply for one-way rentals. Please contact us for details.',
    },
    {
      question: 'What happens if the car breaks down?',
      answer: 'All our vehicles are regularly maintained. In case of breakdown, call our 24/7 support hotline. We provide roadside assistance and replacement vehicle if needed at no extra cost.',
    },
    {
      question: 'Do you provide cars for long-distance trips?',
      answer: 'Absolutely! We specialize in long-distance trips across Nepal. Our vehicles are perfect for journeys from Kathmandu to Pokhara, Chitwan, and other destinations. Weekly and monthly packages are available.',
    },
    {
      question: 'What is the minimum age to rent a car?',
      answer: 'The minimum age is 21 years for self-drive rentals. You must have a valid driving license. For driver-included rentals, there is no age restriction.',
    },
    {
      question: 'Do you offer discounts for long-term rentals?',
      answer: 'Yes! We offer special discounts for weekly (15% off) and monthly (25% off) rentals. Contact us for custom quotes for extended periods.',
    },
    {
      question: 'Can tourists rent cars in Nepal?',
      answer: 'Yes, tourists can rent cars in Nepal. For self-drive, an International Driving Permit (IDP) is recommended. We also help with tourist permits and documentation. Driver-included option is perfect for tourists.',
    },
  ];

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Frequently Asked Questions</h1>
          <p className="text-xl text-primary-100 max-w-3xl">
            Find answers to common questions about our car rental service
          </p>
        </div>
      </section>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="space-y-4">
          {faqs.map((faq, index) => (
            <div key={index} className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
              <button
                onClick={() => setOpenIndex(openIndex === index ? null : index)}
                className="w-full px-6 py-4 flex justify-between items-center text-left hover:bg-neutral-50 transition-colors"
              >
                <span className="font-semibold text-neutral-900 pr-4">{faq.question}</span>
                {openIndex === index ? (
                  <ChevronUp className="h-5 w-5 text-primary-600 flex-shrink-0" />
                ) : (
                  <ChevronDown className="h-5 w-5 text-primary-600 flex-shrink-0" />
                )}
              </button>
              {openIndex === index && (
                <div className="px-6 pb-4 text-neutral-600 leading-relaxed">
                  {faq.answer}
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Still Have Questions */}
        <div className="mt-12 bg-primary-50 rounded-lg p-8 text-center">
          <h2 className="text-2xl font-bold text-neutral-900 mb-4">Still have questions?</h2>
          <p className="text-neutral-600 mb-6">
            Can't find the answer you're looking for? Please contact our friendly team.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <a
              href="https://wa.me/9779800000000"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center justify-center px-6 py-3 bg-green-500 hover:bg-green-600 text-white rounded-lg font-medium transition-colors"
            >
              WhatsApp Us
            </a>
            <a
              href="tel:+9779800000000"
              className="inline-flex items-center justify-center px-6 py-3 bg-primary-600 hover:bg-primary-700 text-white rounded-lg font-medium transition-colors"
            >
              Call Us
            </a>
            <a
              href="/contact"
              className="inline-flex items-center justify-center px-6 py-3 border-2 border-primary-600 text-primary-600 hover:bg-primary-50 rounded-lg font-medium transition-colors"
            >
              Contact Form
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FAQ;
