const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const { defineSecret } = require("firebase-functions/params");

const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const twilio = require("twilio");

// only load dotenv locally
if (process.env.NODE_ENV !== "production") {
  require("dotenv").config();
}

setGlobalOptions({ region: "us-central1" });

admin.initializeApp();

const gmailEmail = defineSecret("GMAIL_EMAIL");
const gmailPassword = defineSecret("GMAIL_PASSWORD");

const twilioAccountSid = defineSecret("TWILIO_ACCOUNT_SID");
const twilioAuthToken = defineSecret("TWILIO_AUTH_TOKEN");
const twilioPhoneNumber = defineSecret("TWILIO_PHONE_NUMBER");


// helper to fetch user profile
async function getUserProfile(userId) {
  const userDoc = await admin.firestore().collection("users").doc(userId).get();

  if (!userDoc.exists) {
    return null;
  }

  return userDoc.data();
}

// helper to create transporter
function createTransporter() {
  const email = process.env.GMAIL_EMAIL || gmailEmail.value();
  const password = process.env.GMAIL_PASSWORD || gmailPassword.value();

  return {
    senderEmail: email,
    transporter: nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: email,
        pass: password,
      },
    }),
  };
}

// helper to create twilio client
function createTwilioClient() {
  const accountSid = process.env.TWILIO_ACCOUNT_SID || twilioAccountSid.value();
  const authToken = process.env.TWILIO_AUTH_TOKEN || twilioAuthToken.value();
  const fromPhone = process.env.TWILIO_PHONE_NUMBER || twilioPhoneNumber.value();

  return {
    fromPhone,
    client: twilio(accountSid, authToken),
  };
}


// send an email when a reservation is made
exports.sendReservationEmail = onDocumentCreated(
  {
    document: "reservations/{reservationId}",
    secrets: [gmailEmail, gmailPassword],
  },
  async (event) => {
    if (!event.data) return;

    const reservation = event.data.data();
    const reservationId = event.params.reservationId;

    if (!reservation?.userId) {
      console.log(`Reservation ${reservationId} has no userId`);
      return;
    }

    try {
      const userData = await getUserProfile(reservation.userId);

      if (!userData) {
        console.log(`No user found for userId: ${reservation.userId}`);
        return;
      }

      const recipientEmail = userData?.email;

      if (!recipientEmail) {
        console.log(`User ${reservation.userId} does not have an email`);
        return;
      }

      const { senderEmail, transporter } = createTransporter();

      const mailOptions = {
        from: senderEmail,
        to: recipientEmail,
        subject: `Reservation Confirmation - ${reservation.eventName ?? "N/A"}`,
        text:
          `Hello,\n\n` +
          `Your reservation for ${reservation.eventName ?? "N/A"} has been confirmed.\n\n` +

          `Here are your details:\n` +
          `• Event: ${reservation.eventName ?? reservation.eventId ?? "N/A"}\n` +
          `• Date: ${reservation.eventDate ?? "N/A"}\n` +
          `• Tickets: ${reservation.numberOfTickets ?? "N/A"}\n\n` +

          `Your reservation ID is: ${reservationId}\n\n` +

          `We’re excited to have you there!\n\n` +

          `If you wish to cancel, you can do so using the "My Reservations" page on the app.\n\n` +

          `See you soon!\n` +
          `— The CloudTicketReservation Team`,
      };

      await transporter.sendMail(mailOptions);
      console.log(`Confirmation email sent to ${recipientEmail}`);
    } catch (error) {
      console.error("Error sending reservation email:", error);
    }
  }
);

// sms text confirmation
exports.sendReservationSms = onDocumentCreated(
  {
    document: "reservations/{reservationId}",
    secrets: [twilioAccountSid, twilioAuthToken, twilioPhoneNumber],
  },
  async (event) => {
    if (!event.data) return;

    const reservation = event.data.data();
    const reservationId = event.params.reservationId;

    if (!reservation?.userId) {
      console.log(`Reservation ${reservationId} has no userId`);
      return;
    }

    try {
      const userData = await getUserProfile(reservation.userId);

      if (!userData) {
        console.log(`No user found for userId: ${reservation.userId}`);
        return;
      }

      const recipientPhone = userData?.phone;

      if (!recipientPhone) {
        console.log(`User ${reservation.userId} does not have a phone number`);
        return;
      }

      const { fromPhone, client } = createTwilioClient();

      const smsBody =
        `Hello! Your reservation for ${reservation.eventName ?? "N/A"} on ${reservation.eventDate ?? "N/A"} ` +
        `has been confirmed. Tickets: ${reservation.numberOfTickets ?? "N/A"}. ` +
        `Reservation ID: ${reservationId}.`;

      await client.messages.create({
        body: smsBody,
        from: fromPhone,
        to: recipientPhone,
      });

      console.log(`Confirmation SMS sent to ${recipientPhone}`);
    } catch (error) {
      console.error("Error sending reservation SMS:", error);
    }
  }
);

// email and sms on cancellation
exports.sendCancellationNotifications = onDocumentUpdated(
  {
    document: "reservations/{reservationId}",
    secrets: [
      gmailEmail,
      gmailPassword,
      twilioAccountSid,
      twilioAuthToken,
      twilioPhoneNumber,
    ],
  },
  async (event) => {
    if (!event.data) return;

    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();
    const reservationId = event.params.reservationId;

    if (!afterData?.userId) {
      console.log(`Reservation ${reservationId} has no userId`);
      return;
    }

    // only trigger when reservation becomes Cancelled
    if (beforeData?.status === "Cancelled" || afterData?.status !== "Cancelled") {
      return;
    }

    try {
      const userData = await getUserProfile(afterData.userId);

      if (!userData) {
        console.log(`No user found for userId: ${afterData.userId}`);
        return;
      }

      const recipientEmail = userData?.email;
      const recipientPhone = userData?.phone;

      // send an email if the email exists
      if (recipientEmail) {
        const { senderEmail, transporter } = createTransporter();

        const mailOptions = {
          from: senderEmail,
          to: recipientEmail,
          subject: `Reservation Cancelled - ${afterData.eventName ?? "N/A"}`,
          text:
            `Hello,\n\n` +
            `Your reservation for ${afterData.eventName ?? "N/A"} has been cancelled.\n\n` +

            `Here are the cancelled reservation details:\n` +
            `• Event: ${afterData.eventName ?? afterData.eventId ?? "N/A"}\n` +
            `• Date: ${afterData.eventDate ?? "N/A"}\n` +
            `• Tickets: ${afterData.numberOfTickets ?? "N/A"}\n\n` +

            `Your reservation ID was: ${reservationId}\n\n` +

            `If this cancellation was made by mistake, you can create a new reservation from the app.\n\n` +

            `Best regards,\n` +
            `— The CloudTicketReservation Team`,
        };

        await transporter.sendMail(mailOptions);
        console.log(`Cancellation email sent to ${recipientEmail}`);
      } else {
        console.log(`User ${afterData.userId} does not have an email`);
      }

      // send sms if the account has a phone number
      if (recipientPhone) {
        const { fromPhone, client } = createTwilioClient();

        const smsBody =
          `Hello! Your reservation for ${afterData.eventName ?? "N/A"} on ${afterData.eventDate ?? "N/A"} ` +
          `has been cancelled. Reservation ID: ${reservationId}.\n\n` +
          `You can make a new reservation anytime using the app.\n\n` +
          `— The CloudTicketReservation Team`;

        await client.messages.create({
          body: smsBody,
          from: fromPhone,
          to: recipientPhone,
        });

        console.log(`Cancellation SMS sent to ${recipientPhone}`);
      } else {
        console.log(`User ${afterData.userId} does not have a phone number`);
      }
    } catch (error) {
      console.error("Error sending cancellation notifications:", error);
    }
  }
);