package com.example.patterns;

/**
 * Demonstrates the Builder Pattern in Java 21
 * The Builder Pattern separates the construction of a complex object from its representation,
 * allowing the same construction process to create different representations.
 */

public class BuilderPatternExample {

    static class Email {
        // Required properties
        private final String from;
        private final String to;
        private final String subject;
        private final String body;

        //Optional properties
        private final String cc;
        private final String bcc;
        private final boolean highPriority;
        private final boolean requestReadReceipt;
        private final String[] attachments;

        // Private constructor - only accessible through the Builder
        private Email(Builder builder) {
            this.from = builder.from;
            this.to = builder.to;
            this.subject = builder.subject;
            this.body = builder.body;
            this.cc = builder.cc;
            this.bcc = builder.bcc;
            this.highPriority = builder.highPriority;
            this.requestReadReceipt = builder.requestReadReceipt;
            this.attachments = builder.attachments;
        }

        // Getters
        public String getFrom() {return from; }
        public String getTo() {return to; }
        public String getSubject() {return subject; }
        public String getBody() {return body; }
        public String getCc() {return cc; }
        public String getBcc() {return bcc; }
        public boolean isHighPriority() {return highPriority; }
        public boolean isRequestReadReceipt() {return requestReadReceipt; }
        public String[] getAttachments() {return attachments; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Email from: ").append(from)
                    .append("\nTo: ").append(to);

            if (cc != null) sb.append("\nCc: ").append(cc);
            if (bcc != null) sb.append("\nBcc: ").append(bcc);
            sb.append("\nSubject: ").append(subject)
                    .append("\nPriority: ").append(highPriority ? "HIGH" : "Normal")
                    .append("\nRead Receipt: ").append(requestReadReceipt ? "Requested" : "Not requested")
                    .append("\nBody: ").append(body);

            if (attachments != null && attachments.length > 0) {
                sb.append("\nAttachments: ");
                for (String attachment : attachments) {
                    sb.append("\n  - ").append(attachment);
                }
            }

            return sb.toString();
        }

        // Builder class - handles the construction of the Email object
        public static class Builder {
            // Required parameters
            private final String from;
            private final String to;
            private final String subject;
            private final String body;

            // Optional parameters with default values
            private String cc = null;
            private String bcc = null;
            private boolean highPriority = false;
            private boolean requestReadReceipt = false;
            private String[] attachments = null;

            // Constructor with required parameters
            public Builder(String from, String to, String subject, String body) {
                this.from = from;
                this.to = to;
                this.subject = subject;
                this.body = body;
            }

            public Builder cc(String cc) {
                this.cc = cc;
                return this;
            }

            public Builder bcc(String bcc) {
                this.bcc = bcc;
                return this;
            }

            public Builder highPriority(boolean highPriority) {
                this.highPriority = highPriority;
                return this;
            }

            public Builder requestReadReceipt(boolean requestReadReceipt) {
                this.requestReadReceipt = requestReadReceipt;
                return this;
            }

            public Builder attachments(String[] attachments){
                this.attachments = attachments;
                return this;
            }

            // Build method to create the final object
            public Email build() {
                return new Email(this);
            }
        }
    }

    // Demonstrate Java 21 record-based builder approach
    public record EmailTemplate(
            String subject,
            String body,
            String signature,
            boolean includeDisclaimer,
            boolean includeCompanyLogo
    ) {
        // Builder pattern implemented with a static inner record
        public static class Builder {
            private String subject;
            private String body;
            private String signature = "";
            private boolean includeDisclaimer = false;
            private boolean includeCompanyLogo = false;

            public Builder subject(String subject) {
                this.subject = subject;
                return this;
            }

            public Builder body(String body) {
                this.body = body;
                return this;
            }

            public Builder signature(String signature) {
                this.signature = signature;
                return this;
            }

            public Builder includeDisclaimer(boolean includeDisclaimer) {
                this.includeDisclaimer = includeDisclaimer;
                return this;
            }

            public Builder includeCompanyLogo(boolean includeCompanyLogo) {
                this.includeCompanyLogo = includeCompanyLogo;
                return this;
            }

            public EmailTemplate build() {
                if (subject == null || body == null) {
                    throw new IllegalStateException("Subject and body are required");
                }
                return new EmailTemplate(subject, body, signature, includeDisclaimer, includeCompanyLogo);
            }
        }

        // Factory method to create a builder
        public static Builder builder() {
            return new Builder();
        }

        // Method to format template into string
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("SUBJECT: ").append(subject).append("\n\n");
            sb.append(body).append("\n");

            if(!signature.isEmpty()) {
                sb.append("\n").append(signature);
            }
            if (includeDisclaimer) {
                sb.append("\n\nDISCLAIMER: This email and any files transmitted with it are confidential...");
            }

            if (includeCompanyLogo) {
                sb.append("\n[Company Logo]");
            }

            return sb.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Builder Pattern Example ===");

        // Using the traditional builder pattern
        Email email = new Email.Builder(
                "sender@example.com",
                "recipient@example.com",
                "Meeting Tomorrow",
                "Let's meet tomorrow at 10 o'clock to discuss the project."
        )
                .cc("manager@example.com")
                .highPriority(true)
                .requestReadReceipt(true)
                .attachments(new String[]{"agenda.pdf", "proposal.docx"})
                .build();

        System.out.println("Traditional Builder Pattern Result:");
        System.out.println(email);

        System.out.println("Traditional Builder Pattern Result:");
        System.out.println(email);

        // Using the record-based builder in Java 21
        System.out.println("\nJava 21 Record-Based Builder Result:");
        EmailTemplate template = EmailTemplate.builder()
                .subject("Quarterly Report")
                .body("Please find attached the quarterly report for Q1 2023.")
                .signature("Best regards,\nJohn Doe\nCEO")
                .includeDisclaimer(true)
                .includeCompanyLogo(true)
                .build();

        System.out.println(template.format());

        /*
         * Key Builder Pattern concepts demonstrated:
         * 1. Complex object construction is separated from the object's representation
         * 2. Step-by-step construction of immutable objects
         * 3. Method chaining for a fluent interface
         * 4. Default values for optional parameters
         * 5. Both traditional class-based and modern record-based approaches
         */

    }
}


