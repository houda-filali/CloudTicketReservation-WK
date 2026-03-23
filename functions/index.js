const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const { defineSecret } = require("firebase-functions/params");

const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

// ONLY load dotenv locally
if (process.env.NODE_ENV !== "production") {
  require("dotenv").config();
}

setGlobalOptions({ region: "us-central1" });

admin.initializeApp();

const gmailEmail = defineSecret("GMAIL_EMAIL");
const gmailPassword = defineSecret("GMAIL_PASSWORD");

exports.sendReservationEmail = onDocumentCreated(
  {
    document: "reservations/{reservationId}",
    secrets: [gmailEmail, gmailPassword]
  },
  async (event) => {

    if (!event.data) return;

    const reservation = event.data.data();

    if (!reservation?.email) {
      console.log("No email provided");
      return;
    }

    // 👇 use local OR secret
    const email =
      process.env.GMAIL_EMAIL || gmailEmail.value();

    const password =
      process.env.GMAIL_PASSWORD || gmailPassword.value();

    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: email,
        pass: password
      }
    });

    const mailOptions = {
      from: email,
      to: reservation.email,
      subject: "Reservation Confirmation",
      text: `Your reservation for event ${reservation.eventId} is confirmed.`
    };

    try {
      await transporter.sendMail(mailOptions);
      console.log("Email sent successfully");
    } catch (error) {
      console.error("Error sending email:", error);
    }
  }
);