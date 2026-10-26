<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>S2B Security – Week 43 Status</title>
<style>
  body { margin: 0; padding: 20px; background: #f2f2f2; font-family: Calibri, "Segoe UI", Arial, sans-serif; color: #1a1a1a; }
  .label-internal { font-size: 14px; color: #555; margin-bottom: 8px; }
  .report { max-width: 1100px; margin: 0 auto; background: #fff; border: 2px solid #1b2a3a; }
  table { width: 100%; border-collapse: collapse; table-layout: fixed; }
  td, th { border: 1px solid #1b2a3a; padding: 5px 8px; font-size: 14px; }
  .hdr-dark { background: #0f2233; color: #fff; font-weight: bold; text-align: center; font-size: 15px; }
  .hdr-blue { background: #1f5fa8; color: #fff; font-weight: bold; text-align: center; font-size: 15px; }
  .center { text-align: center; }
  .green { background: #70ad47; text-align: center; color: #1a1a1a; }
  .brand { height: 95px; padding: 10px 20px; border-bottom: 1px solid #1b2a3a; display: flex; justify-content: space-between; align-items: center; }
  .logo-slot { width: 90px; height: 75px; border: 1px dashed #aaa; display: flex; align-items: center; justify-content: center; font-size: 11px; color: #999; }
  .logo-slot img { max-width: 100%; max-height: 100%; }
  ul { margin: 0; padding-left: 22px; }
  li { margin: 4px 0; }
  .summary-cell { vertical-align: middle; padding: 14px 20px; }
  .chart-wrap { padding: 8px; text-align: center; }
  .updates { padding: 16px 10px; height: 200px; vertical-align: top; font-size: 14px; line-height: 1.5; }
  .footer-note { max-width: 1100px; margin: 16px auto 0; font-size: 14px; }
</style>
</head>
<body>

<div class="label-internal">&#128737; Internal</div>

<div class="report">
  <!-- Brand header: drop your official logo files into the two slots -->
  <div class="brand">
    <div class="logo-slot"><!-- <img src="logo-mark.png" alt=""> -->Logo</div>
    <div class="logo-slot" style="width:220px;"><!-- <img src="logo-wordmark.png" alt=""> -->Wordmark</div>
  </div>

  <!-- Key programs + sprint summary -->
  <table>
    <colgroup><col style="width:20%"><col style="width:13.5%"><col style="width:13%"><col style="width:53.5%"></colgroup>
    <tr>
      <td class="hdr-dark" colspan="3">S2B Security</td>
      <td class="hdr-dark">Week 43</td>
    </tr>
    <tr>
      <th class="hdr-dark">Key Programs</th>
      <th class="hdr-dark">Go Live Planned</th>
      <th class="hdr-dark">Current Status</th>
      <th class="hdr-dark">Current Sprint Summary</th>
    </tr>
    <tr>
      <td>QR KSA Metadata phase 1</td>
      <td class="center">Nov-26</td>
      <td class="center">SIT in progress</td>
      <td class="summary-cell" rowspan="4">
        <ul>
          <li>QR KSA Metadata phase 1</li>
          <li>SAML SSO phase 1.</li>
          <li>DB Shield production enablement.</li>
          <li>SME email ,SMS Templates and TMX integration</li>
          <li>Test Automation completion @ 75%.</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td>S2B Security SME Email &amp; SMS Templates</td>
      <td class="center">Nov-26</td>
      <td class="center">Development in progress</td>
    </tr>
    <tr>
      <td>SAML SSO phase 1</td>
      <td class="center">Nov-26</td>
      <td class="center">Testing in progress</td>
    </tr>
    <tr>
      <td>SME Integration ,TMX alert automation</td>
      <td class="center">Nov-26</td>
      <td class="center">Development in progress</td>
    </tr>
  </table>

  <!-- Monthly production release + production services -->
  <table>
    <colgroup><col style="width:34%"><col style="width:13%"><col style="width:24%"><col style="width:29%"></colgroup>
    <tr><td class="hdr-dark" colspan="4">Monthly Production Release (14th Nov 2026)</td></tr>
    <tr>
      <td class="hdr-blue" colspan="2">Monthly Production Release (14 Nov 2026)</td>
      <td class="hdr-blue" colspan="2">Production Services * ( Oct 2026 )</td>
    </tr>
    <tr><td class="center">SIT / UAT</td><td class="green">In progress</td><td class="center">Change related Incidents</td><td class="green">NIL</td></tr>
    <tr><td class="center">Regression, PT Activities</td><td class="green">Yet to start</td><td class="center">PS Incidents breached SLA</td><td class="green">NIL</td></tr>
    <tr><td class="center">Pre-release Activities</td><td class="green">Yet to start</td><td class="center">Risks due for Closure in 30 Days</td><td class="green">NIL</td></tr>
    <tr><td class="center">Production Deployment</td><td class="green">14-Nov</td><td class="center">RFI Responses</td><td class="green">No Overdue</td></tr>
  </table>

  <!-- Sprint status + backlog chart -->
  <table>
    <colgroup><col style="width:20%"><col style="width:13.5%"><col style="width:13.5%"><col style="width:53%"></colgroup>
    <tr>
      <td class="hdr-dark" colspan="3">Current Sprint (26.10) Status</td>
      <td class="hdr-dark">Squad Backlog Metrics</td>
    </tr>
    <tr>
      <th class="hdr-blue">Squad Name</th>
      <th class="hdr-blue">RAG Status</th>
      <th class="hdr-blue">Remarks</th>
      <td class="chart-wrap" rowspan="5">
        <svg viewBox="0 0 440 200" width="100%" style="max-width:440px" font-family="Calibri, Arial, sans-serif">
          <text x="220" y="18" text-anchor="middle" font-size="12" font-weight="bold" fill="#444">Squad / Story Points</text>
          <!-- axes -->
          <line x1="50" y1="160" x2="420" y2="160" stroke="#999"/>
          <text x="42" y="164" text-anchor="end" font-size="11" fill="#555">0</text>
          <text x="42" y="117" text-anchor="end" font-size="11" fill="#555">100</text>
          <text x="42" y="70"  text-anchor="end" font-size="11" fill="#555">200</text>
          <!-- scale: 200 pts = 94px -->
          <rect x="80"  y="117.7" width="70" height="42.3" fill="#70ad47"/>
          <text x="115" y="111" text-anchor="middle" font-size="12" fill="#444">90</text>
          <rect x="195" y="74"    width="70" height="86"   fill="#70ad47"/>
          <text x="230" y="67"  text-anchor="middle" font-size="12" fill="#444">183</text>
          <rect x="310" y="131.3" width="70" height="28.7" fill="#70ad47"/>
          <text x="345" y="125" text-anchor="middle" font-size="12" fill="#444">61</text>
          <text x="115" y="180" text-anchor="middle" font-size="12" fill="#444">Go-Getters</text>
          <text x="230" y="180" text-anchor="middle" font-size="12" fill="#444">Auth-Gaurdians</text>
          <text x="345" y="180" text-anchor="middle" font-size="12" fill="#444">Star-Sentinals</text>
        </svg>
      </td>
    </tr>
    <tr><td class="center">Go-Getters</td><td class="green"></td><td></td></tr>
    <tr><td class="center">Auth-Gaurdians</td><td class="green"></td><td></td></tr>
    <tr><td class="center">Star-Sentinals</td><td class="green"></td><td></td></tr>
    <tr><td>&nbsp;</td><td class="green"></td><td></td></tr>
  </table>

  <!-- Key updates / risks -->
  <table>
    <tr><td class="hdr-dark">Key Updates / Risks</td></tr>
    <tr>
      <td class="updates">
        Enable Straight2Bank with passwordless authentication. Passkeys offer phishing-resistant login with fewer steps, higher success rates, and no reliance on SMS or physical tokens. Testing planned with a pilot group in Nov release.<br>
        DB Shield Production enablement :: In progress ::<br>
        KSA regulatory requirement for bank document validation. Phase 1 is in progress for QR scanning of document metadata details
      </td>
    </tr>
  </table>
</div>

<p class="footer-note">Please find above weekly status report for this week, summarizing key programs, current progress, key updates/risks .</p>

</body>
</html>
