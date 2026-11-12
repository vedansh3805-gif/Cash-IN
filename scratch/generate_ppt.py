import sys
import os
import pptx
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
from pptx.dml.color import RGBColor

prs = pptx.Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank_layout = prs.slide_layouts[6]

# Theme Colors
BG_DARK = RGBColor(15, 23, 42)        # #0F172A
CARD_BG = RGBColor(30, 41, 59)        # #1E293B
CARD_BORDER = RGBColor(51, 65, 85)    # #334155
CARD_LIGHT_BG = RGBColor(26, 36, 56)  # Slightly darker card
TEXT_WHITE = RGBColor(248, 250, 252)  # #F8FAFC
TEXT_MUTED = RGBColor(148, 163, 184)  # #94A3B8
TEXT_DIM = RGBColor(100, 116, 139)    # #64748B
ACCENT_CYAN = RGBColor(56, 189, 248)  # #38BDF8
ACCENT_BLUE = RGBColor(37, 99, 235)   # #2563EB
ACCENT_GREEN = RGBColor(16, 185, 129) # #10B981
ACCENT_AMBER = RGBColor(245, 158, 11) # #F59E0B
ACCENT_RED = RGBColor(239, 68, 68)    # #EF4444

def set_slide_background(slide):
    background = slide.background
    fill = background.fill
    fill.solid()
    fill.fore_color.rgb = BG_DARK

def add_header(slide, title, category="CASHFLOW ERP • FINANCIAL EARLY WARNING SYSTEM"):
    # Category badge
    cat_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.7), Inches(0.3))
    tf_cat = cat_box.text_frame
    tf_cat.word_wrap = True
    tf_cat.margin_left = tf_cat.margin_top = tf_cat.margin_right = tf_cat.margin_bottom = 0
    p_cat = tf_cat.paragraphs[0]
    p_cat.text = category.upper()
    p_cat.font.size = Pt(10)
    p_cat.font.bold = True
    p_cat.font.color.rgb = ACCENT_CYAN
    p_cat.font.name = "Calibri"

    # Slide Title
    title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.7), Inches(11.7), Inches(0.6))
    tf_title = title_box.text_frame
    tf_title.word_wrap = True
    tf_title.margin_left = tf_title.margin_top = tf_title.margin_right = tf_title.margin_bottom = 0
    p_title = tf_title.paragraphs[0]
    p_title.text = title
    p_title.font.size = Pt(22)
    p_title.font.bold = True
    p_title.font.color.rgb = TEXT_WHITE
    p_title.font.name = "Calibri"

def add_card(slide, left, top, width, height, bg_color=CARD_BG, border_color=CARD_BORDER):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = bg_color
    if border_color:
        shape.line.color.rgb = border_color
        shape.line.width = Pt(1)
    else:
        shape.line.fill.background()
    return shape

# ==========================================
# SLIDE 1: TITLE SLIDE
# ==========================================
slide1 = prs.slides.add_slide(blank_layout)
set_slide_background(slide1)

# Main container card
add_card(slide1, Inches(0.8), Inches(0.8), Inches(11.733), Inches(5.9), bg_color=RGBColor(20, 30, 48), border_color=CARD_BORDER)

# Top Badge
badge = add_card(slide1, Inches(1.3), Inches(1.2), Inches(3.2), Inches(0.38), bg_color=RGBColor(30, 58, 95), border_color=ACCENT_CYAN)
tf_b = badge.text_frame
tf_b.vertical_anchor = MSO_ANCHOR.MIDDLE
p_b = tf_b.paragraphs[0]
p_b.text = "REVIEW - 2 • MILESTONE EVALUATION"
p_b.font.size = Pt(11)
p_b.font.bold = True
p_b.font.color.rgb = ACCENT_CYAN
p_b.alignment = PP_ALIGN.CENTER

# Main Title
tb_title = slide1.shapes.add_textbox(Inches(1.3), Inches(1.75), Inches(10.7), Inches(1.2))
tf_t = tb_title.text_frame
tf_t.word_wrap = True
p_t = tf_t.paragraphs[0]
p_t.text = "CashFlow ERP (Cash-IN)"
p_t.font.size = Pt(36)
p_t.font.bold = True
p_t.font.color.rgb = TEXT_WHITE
p_t.font.name = "Calibri"

p_sub = tf_t.add_paragraph()
p_sub.text = "Small Business Liquidity Management & Cash-Flow Early Warning System"
p_sub.font.size = Pt(18)
p_sub.font.color.rgb = ACCENT_CYAN
p_sub.font.name = "Calibri"

# Desc
tb_desc = slide1.shapes.add_textbox(Inches(1.3), Inches(3.1), Inches(10.7), Inches(0.7))
tf_d = tb_desc.text_frame
tf_d.word_wrap = True
p_d = tf_d.paragraphs[0]
p_d.text = "Predictive Runway Trajectory Modeling • Differential Dataset Versioning • Multi-Channel Debtor Outreach Hub"
p_d.font.size = Pt(13)
p_d.font.color.rgb = TEXT_MUTED

# Stack Chips
stack_text = "Tech Stack: Java 21  |  Spring Boot 3.3.5  |  SQLite 3.45 (HikariCP)  |  HTML5 Canvas  |  JavaFX 21 Companion  |  Zero-CDN"
tb_stack = slide1.shapes.add_textbox(Inches(1.3), Inches(3.8), Inches(10.7), Inches(0.4))
tf_s = tb_stack.text_frame
p_s = tf_s.paragraphs[0]
p_s.text = stack_text
p_s.font.size = Pt(12)
p_s.font.bold = True
p_s.font.color.rgb = ACCENT_AMBER

# Divider Line
line = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(1.3), Inches(4.3), Inches(10.7), Inches(0.02))
line.fill.solid()
line.fill.fore_color.rgb = CARD_BORDER
line.line.fill.background()

# Team Members Box
tb_team = slide1.shapes.add_textbox(Inches(1.3), Inches(4.5), Inches(10.7), Inches(1.8))
tf_tm = tb_team.text_frame
tf_tm.word_wrap = True
p_tm_head = tf_tm.paragraphs[0]
p_tm_head.text = "PROJECT TEAM MEMBERS:"
p_tm_head.font.size = Pt(11)
p_tm_head.font.bold = True
p_tm_head.font.color.rgb = TEXT_MUTED

p_m1 = tf_tm.add_paragraph()
p_m1.text = "• Member 1 (Lead Architect & Developer): Vedansh Saxena  |  Reg No: 21BCE1001  |  GitHub: @vedansh3805-gif"
p_m1.font.size = Pt(12)
p_m1.font.bold = True
p_m1.font.color.rgb = TEXT_WHITE

p_m2 = tf_tm.add_paragraph()
p_m2.text = "• Member 2: [Teammate Name]  |  Reg No: 21BCE1042"
p_m2.font.size = Pt(12)
p_m2.font.color.rgb = TEXT_MUTED

p_m3 = tf_tm.add_paragraph()
p_m3.text = "• Member 3: [Teammate Name]  |  Reg No: 21BCE1089"
p_m3.font.size = Pt(12)
p_m3.font.color.rgb = TEXT_MUTED

# Status pill
stat_pill = add_card(slide1, Inches(9.2), Inches(1.2), Inches(2.8), Inches(0.38), bg_color=RGBColor(6, 78, 59), border_color=ACCENT_GREEN)
tf_sp = stat_pill.text_frame
tf_sp.vertical_anchor = MSO_ANCHOR.MIDDLE
p_sp = tf_sp.paragraphs[0]
p_sp.text = "STATUS: 85%-90% COMPLETE"
p_sp.font.size = Pt(11)
p_sp.font.bold = True
p_sp.font.color.rgb = ACCENT_GREEN
p_sp.alignment = PP_ALIGN.CENTER


# ==========================================
# SLIDE 2: IMPLEMENTATION STATUS TABLE
# ==========================================
slide2 = prs.slides.add_slide(blank_layout)
set_slide_background(slide2)
add_header(slide2, "Project Implementation Status & Milestone Verification")

# Status Summary Cards on top
kpi1 = add_card(slide2, Inches(0.8), Inches(1.4), Inches(2.7), Inches(0.7), bg_color=CARD_BG)
p = kpi1.text_frame.paragraphs[0]
p.text = "OVERALL STATUS\n85%–90% Realistic Complete"
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = ACCENT_GREEN

kpi2 = add_card(slide2, Inches(3.8), Inches(1.4), Inches(2.7), Inches(0.7), bg_color=CARD_BG)
p = kpi2.text_frame.paragraphs[0]
p.text = "AUTOMATED TESTS\n52 / 52 Passed (0 Failures)"
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = ACCENT_CYAN

kpi3 = add_card(slide2, Inches(6.8), Inches(1.4), Inches(2.7), Inches(0.7), bg_color=CARD_BG)
p = kpi3.text_frame.paragraphs[0]
p.text = "SPRING CONTROLLERS\n13 Controllers • 32+ APIs"
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = ACCENT_AMBER

kpi4 = add_card(slide2, Inches(9.8), Inches(1.4), Inches(2.7), Inches(0.7), bg_color=CARD_BG)
p = kpi4.text_frame.paragraphs[0]
p.text = "OFFLINE DEPENDENCIES\nZero-CDN / 100% Offline"
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = TEXT_WHITE

# Table
rows = 8
cols = 5
left = Inches(0.8)
top = Inches(2.3)
width = Inches(11.7)
height = Inches(4.7)

table_shape = slide2.shapes.add_table(rows, cols, left, top, width, height)
table = table_shape.table
table.columns[0].width = Inches(0.7)
table.columns[1].width = Inches(2.7)
table.columns[2].width = Inches(4.3)
table.columns[3].width = Inches(1.7)
table.columns[4].width = Inches(2.3)

headers = ["S.No", "Module / Feature", "Planned & Implemented Capabilities", "Completion Status", "Remarks & Verification"]
for j, h in enumerate(headers):
    cell = table.cell(0, j)
    cell.fill.solid()
    cell.fill.fore_color.rgb = RGBColor(30, 58, 95)
    cell.vertical_anchor = MSO_ANCHOR.MIDDLE
    p = cell.text_frame.paragraphs[0]
    p.text = h
    p.font.bold = True
    p.font.size = Pt(11)
    p.font.color.rgb = TEXT_WHITE

table_data = [
    ("01", "CSV Ingestion & Schema Sanitizer", "13-column schema, OpenCSV 5.9 parser, E.164 phone & email regex validation", "100% Complete", "Verified across 3 demo datasets (10, 20, 35 rows)"),
    ("02", "Differential Versioning Engine", "Transaction-keyed delta diffs (ADDED, MODIFIED, REMOVED, UNCHANGED)", "100% Complete", "DataComparisonService verified; delta chart & timeline"),
    ("03", "Forecasting & Warning Service", "7/30/60/90-day cash curve, 3x anomaly detection, liquidity floor alerts", "100% Complete", "Safety floor active (₹50k); recurring obligations included"),
    ("04", "Multi-Channel Debtor Outreach", "Native dialer (tel:), dual-party SMS, billing email, bulk alert dispatch", "100% Complete", "Dual-party audit copy to Shopkeeper; communication_log"),
    ("05", "Decoupled UI Architecture (ADR 2)", "CashFlowUiService interface uniting BackendUiService & MockUiService", "100% Complete", "Standalone desktop launchable in <2s with 0 DB/network"),
    ("06", "REST API & Financial Web Tier", "13 Spring Boot REST controllers, 32+ endpoints for all modules", "90% Complete", "Sub-50ms query latency; CSV reports & multi-filtering"),
    ("07", "What-If Shock Simulator", "In-memory clone projections, 4 preset shock chips, multi-adjustment builder", "92% Complete", "Dual-curve comparison; Day-1 fallback when date omitted")
]

for i, row in enumerate(table_data):
    for j, val in enumerate(row):
        cell = table.cell(i+1, j)
        cell.fill.solid()
        cell.fill.fore_color.rgb = CARD_BG if i % 2 == 0 else RGBColor(22, 32, 50)
        cell.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = cell.text_frame.paragraphs[0]
        p.text = val
        p.font.size = Pt(10)
        if j == 3:
            p.font.bold = True
            p.font.color.rgb = ACCENT_GREEN
        elif j == 0:
            p.font.color.rgb = ACCENT_CYAN
            p.alignment = PP_ALIGN.CENTER
        else:
            p.font.color.rgb = TEXT_WHITE


# ==========================================
# SLIDE 3: SYSTEM ARCHITECTURE
# ==========================================
slide3 = prs.slides.add_slide(blank_layout)
set_slide_background(slide3)
add_header(slide3, "System Design & Decoupled Architectural Patterns")

# 3 Column Cards
c1 = add_card(slide3, Inches(0.8), Inches(1.5), Inches(3.7), Inches(5.4))
tf1 = c1.text_frame
tf1.word_wrap = True
p = tf1.paragraphs[0]
p.text = "1. DUAL PRESENTATION TIER"
p.font.bold = True
p.font.size = Pt(13)
p.font.color.rgb = ACCENT_CYAN

bullets1 = [
    ("Responsive Web Cockpit", "HTML5 Single Page App (index.html) with 10 operational modules and Component 3 Login overlay."),
    ("EnterpriseChart Canvas", "Custom 2D HTML5 Canvas engine (charts.js, <30KB). Zero external CDNs, 100% offline-ready, custom DPI scaling."),
    ("JavaFX 21 Companion", "Native desktop interface with 12 distinct views, zero-scroll 100vh financial monitor grid."),
    ("5 Enterprise Themes", "Dynamic theme engine: Arctic Ledger, Forest, Copper, Ocean, Paper & Ink with instant canvas redraw.")
]
for title, desc in bullets1:
    p_t = tf1.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(11)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf1.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED

c2 = add_card(slide3, Inches(4.8), Inches(1.5), Inches(3.7), Inches(5.4))
tf2 = c2.text_frame
tf2.word_wrap = True
p = tf2.paragraphs[0]
p.text = "2. DECOUPLED BOUNDARY (ADR 2)"
p.font.bold = True
p.font.size = Pt(13)
p.font.color.rgb = ACCENT_AMBER

bullets2 = [
    ("CashFlowUiService Contract", "Java interface defining all financial dashboard, ledger, forecast, and simulation contracts."),
    ("BackendUiService Provider", "Connects presentation views to Spring Boot 3 services and SQLite database."),
    ("MockUiService Provider", "Pure in-memory synthetic provider allowing standalone desktop launch in <2s with 0 database dependencies."),
    ("Clean Inversion of Control", "Views have zero knowledge of database drivers, SQL, or network protocols.")
]
for title, desc in bullets2:
    p_t = tf2.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(11)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf2.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED

c3 = add_card(slide3, Inches(8.8), Inches(1.5), Inches(3.7), Inches(5.4))
tf3 = c3.text_frame
tf3.word_wrap = True
p = tf3.paragraphs[0]
p.text = "3. ENGINES & PERSISTENCE TIER"
p.font.bold = True
p.font.size = Pt(13)
p.font.color.rgb = ACCENT_GREEN

bullets3 = [
    ("ForecastService", "Computes daily opening, expected inflows, outflows, and closing cash trajectory over 7/30/60/90 days."),
    ("Statistical Anomaly Radar", "Z-Score (>2.5) & 3x category spend spike detector flags abnormal disbursements."),
    ("CommunicationService", "Dispatches Phone/SMS/Email notices and writes double-entry audit records to SQLite."),
    ("SQLite 3.45 ACID Storage", "Embedded single-file database (cashflow.db) managed via HikariCP high-performance pool.")
]
for title, desc in bullets3:
    p_t = tf3.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(11)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf3.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 4: FRONTEND MODULES (RUBRIC TRIAD)
# ==========================================
slide4 = prs.slides.add_slide(blank_layout)
set_slide_background(slide4)
add_header(slide4, "Frontend Module Hierarchy & Review-2 Academic Rubric Alignment")

# Core Component Triad Header
top_card = add_card(slide4, Inches(0.8), Inches(1.4), Inches(11.7), Inches(2.2), bg_color=CARD_BG)
tf_top = top_card.text_frame
tf_top.word_wrap = True
p = tf_top.paragraphs[0]
p.text = "CORE COMPONENT TRIAD (ACADEMIC EVALUATION SPECIFICATION)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN

triad_boxes = [
    ("Component 1: Home Page (Dashboard)", "Executive financial cockpit:\n• Header with Logo, Topbar Nav & Live Status\n• 8-card responsive KPI strip (Liquid cash, inflows, expenses, net velocity, etc.)\n• 3 Canvas charts: Dynamics, Exposure, Curve", Inches(1.0), Inches(1.8), Inches(3.6)),
    ("Component 2: Menu / Catalog Page", "Double-Entry Ledger & Receivables:\n• 10-column filterable transactions table\n• 300ms debounced search & status/flow filters\n• Receivables aging schedule (0-90+ days)\n• Direct tel: and mailto: debtor contact pills", Inches(4.85), Inches(1.8), Inches(3.6)),
    ("Component 3: Login Authentication Page", "Session access & persona switcher:\n• Clean modal overlay with input validation\n• 1-Click quick-fills for evaluation:\n  - Admin (admin@cashflow.com)\n  - Auditor (auditor@cashflow.com)\n  - Shopkeeper (shopkeeper@cashflow.com)", Inches(8.7), Inches(1.8), Inches(3.6))
]

for title, desc, l, t, w in triad_boxes:
    b = add_card(slide4, l, t, w, Inches(1.6), bg_color=RGBColor(20, 30, 48), border_color=CARD_BORDER)
    tf = b.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = title
    p.font.bold = True
    p.font.size = Pt(11)
    p.font.color.rgb = TEXT_WHITE
    p2 = tf.add_paragraph()
    p2.text = desc
    p2.font.size = Pt(9.5)
    p2.font.color.rgb = TEXT_MUTED

# Specialized Operational Modules (2x2 Grid)
c_m1 = add_card(slide4, Inches(0.8), Inches(3.8), Inches(5.7), Inches(1.5), bg_color=CARD_BG)
tf = c_m1.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "OUTREACH & RADAR HUB"
p.font.bold = True
p.font.size = Pt(11)
p.font.color.rgb = ACCENT_AMBER
p2 = tf.add_paragraph()
p2.text = "• Outbound Telephone dialer (tel:), dual-party SMS, formal billing statement email.\n• Early Warning Radar flags projected cash deficit dates and 3x spend spikes."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

c_m2 = add_card(slide4, Inches(6.8), Inches(3.8), Inches(5.7), Inches(1.5), bg_color=CARD_BG)
tf = c_m2.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "PREDICTIVE RUNWAY FORECAST"
p.font.bold = True
p.font.size = Pt(11)
p.font.color.rgb = ACCENT_GREEN
p2 = tf.add_paragraph()
p2.text = "• Forward cash trajectory across 7, 30, 60, and 90-day horizons.\n• Plotted dynamically against business safe operating floor (red dashed ₹50,000 threshold)."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

c_m3 = add_card(slide4, Inches(0.8), Inches(5.5), Inches(5.7), Inches(1.5), bg_color=CARD_BG)
tf = c_m3.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "WHAT-IF STRESS SIMULATOR"
p.font.bold = True
p.font.size = Pt(11)
p.font.color.rgb = ACCENT_CYAN
p2 = tf.add_paragraph()
p2.text = "• 4 One-Click Preset Shock Chips: Client Delay, Capex Shock, Demand Dip, Cost Inflation.\n• Dynamic multi-adjustment builder and instantaneous dual-curve graph rendering."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

c_m4 = add_card(slide4, Inches(6.8), Inches(5.5), Inches(5.7), Inches(1.5), bg_color=CARD_BG)
tf = c_m4.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "DATASET LINEAGE & COMPARATOR"
p.font.bold = True
p.font.size = Pt(11)
p.font.color.rgb = TEXT_WHITE
p2 = tf.add_paragraph()
p2.text = "• Ingests CSV revisions non-destructively; computes differential deltas (+Added, ~Mod, -Rem).\n• Displays delta distributions and renders an integrated chronological audit timeline."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 5: MULTI-CHANNEL DEBTOR OUTREACH
# ==========================================
slide5 = prs.slides.add_slide(blank_layout)
set_slide_background(slide5)
add_header(slide5, "Specialized Capability: Multi-Channel Debtor Outreach & Dual-Party Logging", category="INNOVATION HIGHLIGHT (ADR 2, 5 & 6)")

# Top Explanation
exp_card = add_card(slide5, Inches(0.8), Inches(1.4), Inches(11.7), Inches(1.0), bg_color=CARD_BG)
tf_e = exp_card.text_frame
tf_e.word_wrap = True
p = tf_e.paragraphs[0]
p.text = "CORE INNOVATION: DUAL-PARTY SYNCHRONIZATION"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_AMBER
p2 = tf_e.add_paragraph()
p2.text = "Whenever a collection reminder is dispatched, CashFlow dispatches notifications simultaneously to both parties: the Debtor receives the urgent payment breakdown, while the Shopkeeper (business owner) receives an instant verification audit copy for dispute-free bookkeeping."
p2.font.size = Pt(10.5)
p2.font.color.rgb = TEXT_WHITE

# 3 Channel Cards + Audit Trail Card
ch1 = add_card(slide5, Inches(0.8), Inches(2.6), Inches(3.7), Inches(4.3))
tf = ch1.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "1. NATIVE TELEPHONY DIALER"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN
p2 = tf.add_paragraph()
p2.text = "• Action: Launches OS dialer via 'tel:' protocol directly to buyer's phone.\n• Telephony Modal: Displays invoice amount, days overdue, customer contact profile.\n• Call Notes: Records call conversation outcome and promised payment date into permanent ledger."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

ch2 = add_card(slide5, Inches(4.8), Inches(2.6), Inches(3.7), Inches(4.3))
tf = ch2.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "2. DUAL-PARTY SMS REMINDERS"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_GREEN
p2 = tf.add_paragraph()
p2.text = "• Action: Generates formatted SMS with invoice ID, due date, amount, and bank details.\n• Character Budget: Real-time 160-char counter ensuring carrier compatibility.\n• Dual-Party CC: Sends notification to Debtor and dispatches identical audit copy to Shopkeeper."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

ch3 = add_card(slide5, Inches(8.8), Inches(2.6), Inches(3.7), Inches(4.3))
tf = ch3.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "3. FORMAL STATEMENT EMAIL & BULK"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_AMBER
p2 = tf.add_paragraph()
p2.text = "• Action: Compiles structured billing statement with late fees and remittance schedule.\n• Email Dispatch: Sends to debtor's customer_email; shopkeeper auto-CC'd.\n• Bulk Alert: 1-click batch engine parses all overdue accounts and notifies all simultaneously."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 6: UI/UX DEMO (DASHBOARD & FORECAST)
# ==========================================
slide6 = prs.slides.add_slide(blank_layout)
set_slide_background(slide6)
add_header(slide6, "UI/UX Live Demo: Executive Cockpit & Predictive Runway Forecast", category="LIVE SYSTEM DEMONSTRATION")

# Left Column: Dashboard Cockpit
c_dash = add_card(slide6, Inches(0.8), Inches(1.4), Inches(5.7), Inches(5.5))
tf = c_dash.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "EXECUTIVE DASHBOARD COCKPIT (page-dashboard)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN

metrics = [
    ("Current Liquid In-Bank Cash", "₹1,42,850.00", ACCENT_GREEN),
    ("Expected Receivables Inflow", "₹5,80,000.00", TEXT_WHITE),
    ("Scheduled Operating Outflow", "₹4,37,150.00", ACCENT_AMBER),
    ("Net Cash Flow Velocity", "+₹1,42,850.00", ACCENT_GREEN),
    ("Overdue Defaulter Exposure", "₹75,000.00 (PQR Industries)", ACCENT_RED),
    ("Safe Operating Cash Floor", "₹50,000.00 (Configurable)", TEXT_MUTED),
    ("Overall Liquidity Risk Badge", "HEALTHY / SAFE", ACCENT_GREEN)
]
for title, val, col in metrics:
    p_t = tf.add_paragraph()
    p_t.text = "• " + title + ": " + val
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = col

p_chart_note = tf.add_paragraph()
p_chart_note.text = "\nCanvas Visualizations Rendered in Sub-50ms:\n1. Cash Dynamics: Inflows vs Outflows Comparative Bar Chart\n2. Receivables Exposure: Current vs 30/60/90+ Overdue Doughnut\n3. Historical Liquidity Curve: Cumulative bank balance over time"
p_chart_note.font.size = Pt(10)
p_chart_note.font.color.rgb = TEXT_MUTED

# Right Column: Forecast & Anomaly Radar
c_fore = add_card(slide6, Inches(6.8), Inches(1.4), Inches(5.7), Inches(5.5))
tf = c_fore.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "FORWARD CASH FORECAST & RADAR (page-forecast)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_GREEN

fore_items = [
    ("Dynamic Horizon Switcher", "Instant recalculation across 7-Day, 30-Day, 60-Day, and 90-Day predictive forward windows."),
    ("Dashed Safety Threshold Line", "Superimposes ₹50,000 safety floor over projected trajectory, providing visual shortfall alerts."),
    ("Early Shortfall Detection", "Detects liquidity breaches weeks in advance (e.g. Day 25 shortfall warning under delayed debtor collections)."),
    ("Statistical Anomaly Radar", "Z-Score (>2.5) & 3x Multiplier alerts detecting sudden vendor price hikes or irregular payroll spikes."),
    ("Itemized Daily Ledger", "Tabular breakdown of Opening Cash, Expected Inflow, Expected Outflow, and Closing Balance per calendar day.")
]
for title, desc in fore_items:
    p_t = tf.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 7: UI/UX DEMO (OUTREACH & WHAT-IF)
# ==========================================
slide7 = prs.slides.add_slide(blank_layout)
set_slide_background(slide7)
add_header(slide7, "UI/UX Live Demo: Communication Desk & What-If Stress Simulator", category="LIVE SYSTEM DEMONSTRATION")

# Left: Communication Desk
c_comm = add_card(slide7, Inches(0.8), Inches(1.4), Inches(5.7), Inches(5.5))
tf = c_comm.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "DEBTOR RECOVERY DESK (page-communication)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_AMBER

comm_bullets = [
    ("Debtor Contact Directory", "Real-time list of all overdue invoices with customer name, phone number, email, and overdue days."),
    ("Interactive Action Modals", "Dedicated modal popups for Outbound Call (with outcome logging), SMS (with char counter), and Email."),
    ("1-Click Bulk Defaulter Alert", "Dispatches dual-party notifications to all delinquent buyers in a single click with button debouncing."),
    ("Permanent Audit Ledger", "Immutable SQLite communication_log table tracking log_id, transaction_id, recipient, channel, and timestamp.")
]
for title, desc in comm_bullets:
    p_t = tf.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED

# Right: What-If Simulator
c_what = add_card(slide7, Inches(6.8), Inches(1.4), Inches(5.7), Inches(5.5))
tf = c_what.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "WHAT-IF STRESS SIMULATOR (page-whatif)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN

what_bullets = [
    ("4 One-Click Preset Shocks", "🔴 30-Day Client Delay (Major debtors push settlement)\n🔴 Emergency Capex Shock (Immediate ₹1,00,000 repair)\n🔴 Market Demand Dip (25% reduction in inflows)\n🔴 Operational Cost Inflation (15% rise in supplier costs)"),
    ("Dynamic Multi-Adjustment Builder", "Users can add/remove custom combinations of revenue delays, expense spikes, and one-off emergencies."),
    ("Automated Day-1 Fallback", "If target adjustment date is omitted, shocks immediately apply on Day 1 preventing invalid calculations."),
    ("Dual-Trajectory Canvas Chart", "Renders baseline cash curve vs stressed cash curve simultaneously, displaying Net Impact and Min Cash Floor.")
]
for title, desc in what_bullets:
    p_t = tf.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 8: VERIFICATION & TEST METRICS
# ==========================================
slide8 = prs.slides.add_slide(blank_layout)
set_slide_background(slide8)
add_header(slide8, "Test Execution Audit Report: 100% Passing Verification", category="QUALITY ASSURANCE & RELIABILITY")

# 4 Stat Cards
s1 = add_card(slide8, Inches(0.8), Inches(1.4), Inches(2.7), Inches(1.0), bg_color=CARD_BG)
p = s1.text_frame.paragraphs[0]
p.text = "AUTOMATED TESTS\n52 / 52 Passed"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_GREEN
p2 = s1.text_frame.add_paragraph()
p2.text = "100% Pass Rate Across All Suites"
p2.font.size = Pt(9.5)
p2.font.color.rgb = TEXT_MUTED

s2 = add_card(slide8, Inches(3.8), Inches(1.4), Inches(2.7), Inches(1.0), bg_color=CARD_BG)
p = s2.text_frame.paragraphs[0]
p.text = "ASSERTION POINTS\n130+ Verifications"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN
p2 = s2.text_frame.add_paragraph()
p2.text = "Comprehensive end-to-end checks"
p2.font.size = Pt(9.5)
p2.font.color.rgb = TEXT_MUTED

s3 = add_card(slide8, Inches(6.8), Inches(1.4), Inches(2.7), Inches(1.0), bg_color=CARD_BG)
p = s3.text_frame.paragraphs[0]
p.text = "BUILD STATUS\nBUILD SUCCESS"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_AMBER
p2 = s3.text_frame.add_paragraph()
p2.text = "0 Failures  •  0 Errors  •  0 Skipped"
p2.font.size = Pt(9.5)
p2.font.color.rgb = TEXT_MUTED

s4 = add_card(slide8, Inches(9.8), Inches(1.4), Inches(2.7), Inches(1.0), bg_color=CARD_BG)
p = s4.text_frame.paragraphs[0]
p.text = "LATENCY BENCHMARK\n< 50ms API Latency"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = TEXT_WHITE
p2 = s4.text_frame.add_paragraph()
p2.text = "Instantaneous local query execution"
p2.font.size = Pt(9.5)
p2.font.color.rgb = TEXT_MUTED

# Detailed Test Breakdown Cards
t_card1 = add_card(slide8, Inches(0.8), Inches(2.6), Inches(5.7), Inches(4.3))
tf1 = t_card1.text_frame
tf1.word_wrap = True
p = tf1.paragraphs[0]
p.text = "CORE TEST SUITES BREAKDOWN (mvn test)"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN

test_rows = [
    ("ComprehensiveDryRunTest.java (17 Tests)", "Full lifecycle: V1/V2 import, 8-KPI verification, transaction filters, contacts, Call, SMS, Email, Dual-Party broadcast, Bulk alert, Audit trail, Forecast 7/30/60/90D, Anomaly radar, Recurring CRUD, What-If simulation, Version deltas, 5 Themes, CSV report."),
    ("PermutationCombinationTest.java (5 Tests)", "Systematic matrix verifying all 3 demo datasets, compound multi-shocks, blank dates, and multi-channel dispatches."),
    ("BackendUiServiceTest.java (11 Tests)", "Dashboard metrics, aging schedule bucketing, and overdue warnings.")
]
for title, desc in test_rows:
    p_t = tf1.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf1.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = TEXT_MUTED

t_card2 = add_card(slide8, Inches(6.8), Inches(2.6), Inches(5.7), Inches(4.3))
tf2 = t_card2.text_frame
tf2.word_wrap = True
p = tf2.paragraphs[0]
p.text = "ARCHITECTURAL INVARIANTS CONFIRMED"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_GREEN

invariants = [
    ("Zero-CDN Dependency Invariant", "All UI logic, EnterpriseChart canvas routines, stylesheets, and fonts execute locally without remote network fetches."),
    ("Financial Data Immutability Invariant", "What-If simulations clone existing ledger models in memory. 0 records are corrupted or overwritten during stress modeling."),
    ("Safe Schema Evolution Invariant", "DatabaseConfig auto-migrates SQLite tables on launch without destroying prior historical data."),
    ("Dual-Party Compliance Invariant", "Every debtor communication event produces twin audit records (Buyer copy + Shopkeeper CC).")
]
for title, desc in invariants:
    p_t = tf2.add_paragraph()
    p_t.text = "• " + title + ":"
    p_t.font.bold = True
    p_t.font.size = Pt(10.5)
    p_t.font.color.rgb = TEXT_WHITE
    p_d = tf2.add_paragraph()
    p_d.text = "  " + desc
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = TEXT_MUTED


# ==========================================
# SLIDE 9: CONCLUSION & ROADMAP
# ==========================================
slide9 = prs.slides.add_slide(blank_layout)
set_slide_background(slide9)
add_header(slide9, "Review-2 Milestone Conclusion & Future Roadmap", category="CONCLUSION & NEXT STEPS")

# Status Box
st_box = add_card(slide9, Inches(0.8), Inches(1.4), Inches(11.7), Inches(1.2), bg_color=CARD_BG)
tf_s = st_box.text_frame
tf_s.word_wrap = True
p = tf_s.paragraphs[0]
p.text = "MILESTONE STATUS: REVIEW-2 OBJECTIVES VASTLY EXCEEDED"
p.font.bold = True
p.font.size = Pt(13)
p.font.color.rgb = ACCENT_GREEN
p2 = tf_s.add_paragraph()
p2.text = "The platform has progressed well past the initial 50% Review-2 requirements and is currently operating at 85%–90% realistic completion. Core data ingestion, differential versioning, 7/30/60/90-day cash forecasting, statistical anomaly radar, dual-party outreach, and the What-If stress simulator are fully implemented and verified with 52 automated tests."
p2.font.size = Pt(10.5)
p2.font.color.rgb = TEXT_WHITE

# 3 Next Steps
b1 = add_card(slide9, Inches(0.8), Inches(2.8), Inches(3.7), Inches(4.1))
tf = b1.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "1. LIVE GATEWAY WEBHOOKS"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_CYAN
p2 = tf.add_paragraph()
p2.text = "• Target: Razorpay & Stripe integration.\n• Capability: Dynamic payment link injection into SMS/Email statements with automated webhook settlement callback upon customer payment.\n• Impact: Eliminates manual status updates."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

b2 = add_card(slide9, Inches(4.8), Inches(2.8), Inches(3.7), Inches(4.1))
tf = b2.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "2. EXTERNAL TELEPHONY APIS"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_AMBER
p2 = tf.add_paragraph()
p2.text = "• Target: Twilio / AWS SNS carrier integration.\n• Capability: Production-grade SMS and voice delivery beyond the native 'tel:' protocol with real-time SMS delivery receipt webhooks.\n• Impact: Enterprise scale collection desk."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

b3 = add_card(slide9, Inches(8.8), Inches(2.8), Inches(3.7), Inches(4.1))
tf = b3.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "3. OPEN BANKING LIVE FEEDS"
p.font.bold = True
p.font.size = Pt(12)
p.font.color.rgb = ACCENT_GREEN
p2 = tf.add_paragraph()
p2.text = "• Target: Account Aggregator / Plaid sync.\n• Capability: Automatic real-time bank statement synchronization, eliminating manual CSV file uploads.\n• Documentation: Continuous updates to DECISIONS.md and ARCHITECTURE.md."
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_MUTED

output_pptx = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/CashFlow_Review2_Presentation.pptx"
prs.save(output_pptx)
print(f"Successfully generated {output_pptx}")
