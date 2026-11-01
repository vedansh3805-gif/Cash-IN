/* ==========================================================================
   CashFlow — Enterprise Canvas Chart Engine (100% Offline & HiDPI Native)
   ========================================================================== */

const Charts = {
    instances: {},

    getThemeColors() {
        const style = getComputedStyle(document.body);
        const get = (prop, fallback) => {
            const val = style.getPropertyValue(prop).trim();
            return val || fallback;
        };
        return {
            bgCard: get('--bg-card', '#1e293b'),
            bgSurface: get('--bg-surface', '#0f172a'),
            textPrimary: get('--text-primary', '#f8fafc'),
            textSecondary: get('--text-secondary', '#94a3b8'),
            textMuted: get('--text-muted', '#64748b'),
            borderColor: get('--border-color', '#334155'),
            accentPrimary: get('--accent-primary', '#38bdf8'),
            success: get('--success', '#10b981'),
            warning: get('--warning', '#f59e0b'),
            danger: get('--danger', '#ef4444'),
            info: get('--info', '#6366f1'),
            chart1: get('--chart-1', '#38bdf8'),
            chart2: get('--chart-2', '#10b981'),
            chart3: get('--chart-3', '#f59e0b'),
            chart4: get('--chart-4', '#ef4444'),
            chart5: get('--chart-5', '#8b5cf6'),
            chart6: get('--chart-6', '#ec4899'),
        };
    },

    create(canvasId, type, data, customOptions = {}) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return null;

        if (this.instances[canvasId]) {
            this.instances[canvasId].destroy();
        }

        const chartInstance = new EnterpriseChart(canvas, type, data, customOptions, this.getThemeColors());
        this.instances[canvasId] = chartInstance;
        return chartInstance;
    },

    destroyAll() {
        for (const k in this.instances) {
            if (this.instances[k]) this.instances[k].destroy();
        }
        this.instances = {};
    },

    updateAllThemes() {
        const colors = this.getThemeColors();
        for (const k in this.instances) {
            if (this.instances[k]) {
                this.instances[k].theme = colors;
                this.instances[k].render();
            }
        }
    },

    lineDataset(label, data, colorKey = 'chart1') {
        const c = this.getThemeColors();
        const color = c[colorKey] || colorKey;
        return {
            label,
            data,
            borderColor: color,
            backgroundColor: color + '25',
            fill: true,
            borderWidth: 2,
        };
    },

    barDataset(label, data, colorKey = 'chart1') {
        const c = this.getThemeColors();
        const color = c[colorKey] || colorKey;
        return {
            label,
            data,
            backgroundColor: color + 'cc',
            borderColor: color,
            borderWidth: 1,
        };
    },
};

/**
 * Modern HTML5 Canvas Chart Implementation
 */
class EnterpriseChart {
    constructor(canvas, type, data, options, themeColors) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.type = type;
        this.data = data || { labels: [], datasets: [] };
        this.options = options || {};
        this.theme = themeColors;
        this.hoverIndex = -1;
        this.hoverDataset = -1;
        this.mousePos = { x: -1, y: -1 };

        this.initTooltip();
        this.bindEvents();
        this.render();

        // Responsive Resize Observer with debouncing and change detection
        if (window.ResizeObserver && canvas.parentElement) {
            this.lastW = 0;
            this.lastH = 0;
            this.resizeObserver = new ResizeObserver((entries) => {
                let shouldRender = false;
                for (const entry of entries) {
                    const cr = entry.contentRect;
                    if (cr && cr.width > 0 && cr.height > 0) {
                        const dw = Math.abs(cr.width - this.lastW);
                        const dh = Math.abs(cr.height - this.lastH);
                        if (dw >= 2 || dh >= 2) {
                            this.lastW = cr.width;
                            this.lastH = cr.height;
                            shouldRender = true;
                        }
                    }
                }
                if (shouldRender) {
                    if (this.resizeTimer) cancelAnimationFrame(this.resizeTimer);
                    this.resizeTimer = requestAnimationFrame(() => this.render());
                }
            });
            this.resizeObserver.observe(canvas.parentElement);
        }
    }

    initTooltip() {
        let tip = document.getElementById('cf-chart-tooltip');
        if (!tip) {
            tip = document.createElement('div');
            tip.id = 'cf-chart-tooltip';
            tip.style.position = 'fixed';
            tip.style.display = 'none';
            tip.style.pointerEvents = 'none';
            tip.style.zIndex = '9999';
            tip.style.backgroundColor = 'rgba(15, 23, 42, 0.95)';
            tip.style.color = '#f8fafc';
            tip.style.border = '1px solid #334155';
            tip.style.borderRadius = '6px';
            tip.style.padding = '8px 12px';
            tip.style.fontSize = '12px';
            tip.style.boxShadow = '0 10px 25px -5px rgba(0, 0, 0, 0.5)';
            tip.style.backdropFilter = 'blur(4px)';
            tip.style.transition = 'opacity 0.15s ease, transform 0.15s ease';
            document.body.appendChild(tip);
        }
        this.tooltip = tip;
    }

    bindEvents() {
        this.onMouseMove = (e) => {
            const rect = this.canvas.getBoundingClientRect();
            this.mousePos = {
                x: e.clientX - rect.left,
                y: e.clientY - rect.top,
                screenX: e.clientX,
                screenY: e.clientY,
            };
            this.handleHover();
        };

        this.onMouseLeave = () => {
            this.mousePos = { x: -1, y: -1 };
            this.hoverIndex = -1;
            this.hoverDataset = -1;
            this.hideTooltip();
            this.render();
        };

        this.canvas.addEventListener('mousemove', this.onMouseMove);
        this.canvas.addEventListener('mouseleave', this.onMouseLeave);
    }

    showTooltip(html, x, y) {
        if (!this.tooltip) return;
        this.tooltip.innerHTML = html;
        this.tooltip.style.display = 'block';
        this.tooltip.style.left = `${x + 14}px`;
        this.tooltip.style.top = `${y - 20}px`;
        this.tooltip.style.opacity = '1';
    }

    hideTooltip() {
        if (!this.tooltip) return;
        this.tooltip.style.display = 'none';
        this.tooltip.style.opacity = '0';
    }

    destroy() {
        this.canvas.removeEventListener('mousemove', this.onMouseMove);
        this.canvas.removeEventListener('mouseleave', this.onMouseLeave);
        if (this.resizeObserver) {
            this.resizeObserver.disconnect();
            this.resizeObserver = null;
        }
        if (this.resizeTimer) {
            cancelAnimationFrame(this.resizeTimer);
            this.resizeTimer = null;
        }
        this.hideTooltip();
    }

    setupCanvas() {
        const dpr = window.devicePixelRatio || 1;
        const parent = this.canvas.parentElement;
        
        let w = 400;
        let h = 248;

        if (parent) {
            const computed = window.getComputedStyle(parent);
            const padX = (parseFloat(computed.paddingLeft) || 0) + (parseFloat(computed.paddingRight) || 0);
            const padY = (parseFloat(computed.paddingTop) || 0) + (parseFloat(computed.paddingBottom) || 0);
            
            const availW = parent.clientWidth - padX;
            const availH = parent.clientHeight - padY;
            
            w = availW > 20 ? Math.floor(availW) : 400;
            h = availH > 20 ? Math.floor(availH) : (parent.classList.contains('chart-tall') ? 348 : 248);
        } else {
            w = Math.floor(this.canvas.clientWidth || 400);
            h = Math.floor(this.canvas.clientHeight || 248);
        }

        if (this.options && this.options.height) {
            h = this.options.height;
        }

        const pixelW = Math.round(w * dpr);
        const pixelH = Math.round(h * dpr);

        if (this.canvas.width !== pixelW || this.canvas.height !== pixelH) {
            this.canvas.width = pixelW;
            this.canvas.height = pixelH;
        }

        this.canvas.style.width = `${w}px`;
        this.canvas.style.height = `${h}px`;

        this.ctx.resetTransform();
        this.ctx.scale(dpr, dpr);
        return { w, h };
    }

    renderEmptyState(w, h, text = 'No data available for display') {
        this.ctx.save();
        this.ctx.font = '13px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillStyle = (this.theme && this.theme.textMuted) || '#94a3b8';
        this.ctx.fillText(text, w / 2, h / 2);
        this.ctx.restore();
    }

    render() {
        const { w, h } = this.setupCanvas();
        this.theme = Charts.getThemeColors();

        this.ctx.save();
        this.ctx.resetTransform();
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctx.restore();

        if (this.type === 'line') {
            this.renderLineChart(w, h);
        } else if (this.type === 'bar') {
            if (this.options && this.options.indexAxis === 'y') {
                this.renderHorizontalBarChart(w, h);
            } else {
                this.renderBarChart(w, h);
            }
        } else if (this.type === 'doughnut' || this.type === 'pie') {
            this.renderDoughnutChart(w, h);
        }
    }

    /* -------------------------------------------------------------
       LINE CHART RENDERER
       ------------------------------------------------------------- */
    renderLineChart(w, h) {
        const labels = this.data.labels || [];
        const datasets = this.data.datasets || [];
        if (labels.length === 0 || datasets.length === 0) {
            this.renderEmptyState(w, h, 'No trajectory points to plot');
            return;
        }

        const padLeft = 70;
        const padRight = 24;
        const padTop = 24;
        const padBottom = 36;
        const plotW = w - padLeft - padRight;
        const plotH = h - padTop - padBottom;

        // Find min & max across all datasets
        let minVal = Infinity;
        let maxVal = -Infinity;
        datasets.forEach(ds => {
            (ds.data || []).forEach(val => {
                if (val != null && !isNaN(val)) {
                    if (val < minVal) minVal = val;
                    if (val > maxVal) maxVal = val;
                }
            });
        });

        if (minVal === Infinity) { minVal = 0; maxVal = 100; }
        if (minVal > 0) minVal = 0; // Baseline from 0 for readability
        if (maxVal <= minVal) maxVal = minVal + 1000;

        // Add 10% headroom
        const range = maxVal - minVal;
        maxVal += range * 0.1;

        const getY = (val) => padTop + plotH - ((val - minVal) / (maxVal - minVal)) * plotH;
        const getX = (i) => padLeft + (i / Math.max(1, labels.length - 1)) * plotW;

        // Draw horizontal grid lines & Y ticks
        this.ctx.font = '11px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
        this.ctx.textAlign = 'right';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillStyle = this.theme.textMuted;
        this.ctx.strokeStyle = this.theme.borderColor;
        this.ctx.lineWidth = 0.5;

        const steps = 4;
        for (let s = 0; s <= steps; s++) {
            const val = minVal + (s / steps) * (maxVal - minVal);
            const y = getY(val);

            this.ctx.beginPath();
            this.ctx.moveTo(padLeft, y);
            this.ctx.lineTo(padLeft + plotW, y);
            this.ctx.stroke();

            this.ctx.fillText(Utils.formatCurrency(val), padLeft - 10, y);
        }

        // Draw baseline at 0 if within range
        if (minVal < 0 && maxVal > 0) {
            const y0 = getY(0);
            this.ctx.beginPath();
            this.ctx.strokeStyle = 'rgba(239, 68, 68, 0.5)';
            this.ctx.lineWidth = 1.2;
            this.ctx.setLineDash([4, 2]);
            this.ctx.moveTo(padLeft, y0);
            this.ctx.lineTo(padLeft + plotW, y0);
            this.ctx.stroke();
            this.ctx.setLineDash([]);
        }

        // Draw X-axis labels (subsample if too many)
        const stepX = Math.max(1, Math.ceil(labels.length / 8));
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'top';
        for (let i = 0; i < labels.length; i += stepX) {
            const x = getX(i);
            this.ctx.fillText(labels[i], x, padTop + plotH + 8);
        }

        // Render Datasets
        datasets.forEach((ds) => {
            const d = ds.data || [];
            if (d.length === 0) return;

            const color = ds.borderColor || this.theme.accentPrimary;

            // Area fill
            if (ds.fill) {
                this.ctx.beginPath();
                this.ctx.moveTo(getX(0), getY(d[0] || 0));
                for (let i = 1; i < d.length; i++) {
                    this.ctx.lineTo(getX(i), getY(d[i] || 0));
                }
                this.ctx.lineTo(getX(d.length - 1), getY(minVal));
                this.ctx.lineTo(getX(0), getY(minVal));
                this.ctx.closePath();

                const grad = this.ctx.createLinearGradient(0, padTop, 0, padTop + plotH);
                grad.addColorStop(0, color + '40');
                grad.addColorStop(1, color + '02');
                this.ctx.fillStyle = grad;
                this.ctx.fill();
            }

            // Stroke line
            this.ctx.beginPath();
            if (ds.borderDash) {
                this.ctx.setLineDash(ds.borderDash);
            } else {
                this.ctx.setLineDash([]);
            }
            this.ctx.strokeStyle = color;
            this.ctx.lineWidth = ds.borderWidth || 2;
            for (let i = 0; i < d.length; i++) {
                const x = getX(i);
                const y = getY(d[i] || 0);
                if (i === 0) this.ctx.moveTo(x, y);
                else this.ctx.lineTo(x, y);
            }
            this.ctx.stroke();
            this.ctx.setLineDash([]);

            // Points
            if (ds.pointRadius !== 0) {
                for (let i = 0; i < d.length; i++) {
                    const x = getX(i);
                    const y = getY(d[i] || 0);
                    const isHover = (this.hoverIndex === i);

                    this.ctx.beginPath();
                    this.ctx.arc(x, y, isHover ? 5 : 3, 0, Math.PI * 2);
                    this.ctx.fillStyle = isHover ? '#ffffff' : color;
                    this.ctx.fill();
                    this.ctx.strokeStyle = color;
                    this.ctx.lineWidth = 1.5;
                    this.ctx.stroke();
                }
            }
        });

        // Hover Guide Line
        if (this.hoverIndex >= 0 && this.hoverIndex < labels.length) {
            const hx = getX(this.hoverIndex);
            this.ctx.beginPath();
            this.ctx.setLineDash([4, 4]);
            this.ctx.strokeStyle = this.theme.textMuted;
            this.ctx.lineWidth = 1;
            this.ctx.moveTo(hx, padTop);
            this.ctx.lineTo(hx, padTop + plotH);
            this.ctx.stroke();
            this.ctx.setLineDash([]);
        }

        this.lineGeometry = { padLeft, padRight, padTop, padBottom, plotW, plotH, labels, datasets };
    }

    /* -------------------------------------------------------------
       BAR CHART RENDERER (VERTICAL)
       ------------------------------------------------------------- */
    renderBarChart(w, h) {
        const labels = this.data.labels || [];
        const datasets = this.data.datasets || [];
        if (labels.length === 0 || datasets.length === 0) {
            this.renderEmptyState(w, h, 'No data series available');
            return;
        }

        const padLeft = 65;
        const padRight = 20;
        const padTop = 20;
        const padBottom = 36;
        const plotW = w - padLeft - padRight;
        const plotH = h - padTop - padBottom;

        let maxVal = 0;
        datasets.forEach(ds => {
            (ds.data || []).forEach(v => {
                if (v > maxVal) maxVal = v;
            });
        });
        if (maxVal === 0) maxVal = 1000;
        maxVal *= 1.15;

        // Y Grid
        this.ctx.font = '11px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
        this.ctx.textAlign = 'right';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillStyle = this.theme.textMuted;
        this.ctx.strokeStyle = this.theme.borderColor;
        this.ctx.lineWidth = 0.5;

        const steps = 4;
        for (let s = 0; s <= steps; s++) {
            const val = (s / steps) * maxVal;
            const y = padTop + plotH - (val / maxVal) * plotH;

            this.ctx.beginPath();
            this.ctx.moveTo(padLeft, y);
            this.ctx.lineTo(padLeft + plotW, y);
            this.ctx.stroke();

            this.ctx.fillText(Utils.formatCurrency(val), padLeft - 8, y);
        }

        // Draw Bars
        const groupW = plotW / labels.length;
        const barPad = Math.max(4, groupW * 0.15);
        const totalBarW = groupW - barPad * 2;
        const barW = totalBarW / datasets.length;

        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'top';

        labels.forEach((label, i) => {
            const groupX = padLeft + i * groupW + barPad;

            // X label
            this.ctx.fillStyle = this.theme.textMuted;
            this.ctx.fillText(label, groupX + totalBarW / 2, padTop + plotH + 8);

            datasets.forEach((ds, dsi) => {
                const val = (ds.data && ds.data[i] != null) ? ds.data[i] : 0;
                const barH = (val / maxVal) * plotH;
                const bx = groupX + dsi * barW;
                const by = padTop + plotH - barH;

                const color = Array.isArray(ds.backgroundColor) ? ds.backgroundColor[i] : ds.backgroundColor || this.theme.accentPrimary;

                this.ctx.fillStyle = color;
                this.roundRect(bx, by, barW - 2, barH, 4, true);

                if (this.hoverIndex === i && this.hoverDataset === dsi) {
                    this.ctx.strokeStyle = '#ffffff';
                    this.ctx.lineWidth = 1.5;
                    this.roundRect(bx, by, barW - 2, barH, 4, false, true);
                }
            });
        });

        this.barGeometry = { padLeft, padTop, plotW, plotH, labels, datasets, groupW, barPad, barW };
    }

    /* -------------------------------------------------------------
       HORIZONTAL BAR CHART RENDERER
       ------------------------------------------------------------- */
    renderHorizontalBarChart(w, h) {
        const labels = this.data.labels || [];
        const datasets = this.data.datasets || [];
        if (labels.length === 0 || datasets.length === 0) {
            this.renderEmptyState(w, h, 'No debtor delay records');
            return;
        }

        const padLeft = 110;
        const padRight = 30;
        const padTop = 15;
        const padBottom = 25;
        const plotW = w - padLeft - padRight;
        const plotH = h - padTop - padBottom;

        let maxVal = 0;
        datasets.forEach(ds => {
            (ds.data || []).forEach(v => {
                if (v > maxVal) maxVal = v;
            });
        });
        if (maxVal === 0) maxVal = 10;
        maxVal *= 1.15;

        const rowH = plotH / labels.length;
        const barH = Math.min(22, rowH * 0.6);

        this.ctx.font = '11px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';

        labels.forEach((label, i) => {
            const y = padTop + i * rowH + (rowH - barH) / 2;

            // Y label (Customer Name)
            this.ctx.textAlign = 'right';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillStyle = this.theme.textPrimary;
            this.ctx.fillText(Utils.truncate(label, 14), padLeft - 10, y + barH / 2);

            // Bar background
            this.ctx.fillStyle = this.theme.borderColor + '40';
            this.roundRect(padLeft, y, plotW, barH, 3, true);

            // Actual Bar
            const ds = datasets[0];
            const val = (ds && ds.data && ds.data[i] != null) ? ds.data[i] : 0;
            const barW = (val / maxVal) * plotW;
            const color = ds.backgroundColor || this.theme.warning;

            this.ctx.fillStyle = color;
            this.roundRect(padLeft, y, Math.max(2, barW), barH, 3, true);

            // Metric label at bar tip
            this.ctx.textAlign = 'left';
            this.ctx.fillStyle = this.theme.textMuted;
            this.ctx.fillText(`${val} d`, padLeft + Math.max(2, barW) + 6, y + barH / 2);
        });

        this.hBarGeometry = { padLeft, padTop, plotW, plotH, labels, datasets, rowH, barH, maxVal };
    }

    /* -------------------------------------------------------------
       DOUGHNUT & PIE CHART RENDERER
       ------------------------------------------------------------- */
    renderDoughnutChart(w, h) {
        const labels = this.data.labels || [];
        const dataset = (this.data.datasets && this.data.datasets[0]) || { data: [] };
        const data = dataset.data || [];
        if (data.length === 0) {
            this.renderEmptyState(w, h, 'No category metrics');
            return;
        }

        const total = data.reduce((sum, v) => sum + (Number(v) || 0), 0);
        if (total === 0) {
            this.renderEmptyState(w, h, 'Zero balance');
            return;
        }

        const pad = 16;
        const cx = w * 0.38;
        const cy = h / 2;
        const radius = Math.min(cx - pad, cy - pad);
        const innerRadius = this.type === 'doughnut' ? radius * 0.58 : 0;

        const colors = dataset.backgroundColor || [
            this.theme.chart1, this.theme.chart2, this.theme.chart3,
            this.theme.chart4, this.theme.chart5, this.theme.chart6
        ];

        let startAngle = -Math.PI / 2;
        this.segments = [];

        data.forEach((val, i) => {
            const sliceAngle = ((Number(val) || 0) / total) * Math.PI * 2;
            const endAngle = startAngle + sliceAngle;
            const isHover = (this.hoverIndex === i);
            const r = isHover ? radius + 4 : radius;

            this.ctx.beginPath();
            this.ctx.arc(cx, cy, r, startAngle, endAngle);
            if (innerRadius > 0) {
                this.ctx.arc(cx, cy, innerRadius, endAngle, startAngle, true);
            } else {
                this.ctx.lineTo(cx, cy);
            }
            this.ctx.closePath();

            const c = Array.isArray(colors) ? colors[i % colors.length] : colors;
            this.ctx.fillStyle = c;
            this.ctx.fill();
            this.ctx.strokeStyle = this.theme.bgCard;
            this.ctx.lineWidth = 2;
            this.ctx.stroke();

            this.segments.push({ startAngle, endAngle, val, label: labels[i] || '', color: c });
            startAngle = endAngle;
        });

        // Center total text for doughnut
        if (this.type === 'doughnut') {
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.font = 'bold 13px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
            this.ctx.fillStyle = this.theme.textPrimary;
            this.ctx.fillText(Utils.formatCurrency(total), cx, cy - 6);
            this.ctx.font = '10px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
            this.ctx.fillStyle = this.theme.textMuted;
            this.ctx.fillText('TOTAL', cx, cy + 12);
        }

        // Draw Legend on the right side
        const legX = w * 0.70;
        const startY = Math.max(12, cy - (labels.length * 20) / 2);
        this.ctx.font = '11px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
        this.ctx.textAlign = 'left';
        this.ctx.textBaseline = 'middle';

        labels.forEach((label, i) => {
            const ly = startY + i * 22;
            const c = Array.isArray(colors) ? colors[i % colors.length] : colors;
            const pct = Math.round(((Number(data[i]) || 0) / total) * 100);

            // Color box
            this.ctx.fillStyle = c;
            this.roundRect(legX, ly - 5, 10, 10, 2, true);

            // Label & percentage
            this.ctx.fillStyle = this.theme.textSecondary;
            this.ctx.fillText(`${Utils.truncate(label, 12)} (${pct}%)`, legX + 16, ly);
        });

        this.doughnutGeometry = { cx, cy, radius, innerRadius };
    }

    /* -------------------------------------------------------------
       INTERACTIVE HOVER & TOOLTIP HANDLERS
       ------------------------------------------------------------- */
    handleHover() {
        if (this.type === 'line' && this.lineGeometry) {
            const { padLeft, plotW, labels, datasets } = this.lineGeometry;
            if (this.mousePos.x < padLeft || this.mousePos.x > padLeft + plotW) {
                this.hideTooltip();
                return;
            }
            const relX = this.mousePos.x - padLeft;
            const idx = Math.round((relX / plotW) * (labels.length - 1));
            if (idx >= 0 && idx < labels.length && idx !== this.hoverIndex) {
                this.hoverIndex = idx;
                this.render();

                let html = `<div style="font-weight:600;margin-bottom:4px;color:${this.theme.accentPrimary}">${labels[idx]}</div>`;
                datasets.forEach(ds => {
                    const val = (ds.data && ds.data[idx] != null) ? ds.data[idx] : 0;
                    html += `
                        <div style="display:flex;justify-content:space-between;gap:12px;margin-top:2px;">
                            <span style="color:${this.theme.textSecondary}">${ds.label || 'Value'}:</span>
                            <strong style="color:#ffffff">${Utils.formatCurrency(val)}</strong>
                        </div>
                    `;
                });
                this.showTooltip(html, this.mousePos.screenX, this.mousePos.screenY);
            }
        } else if (this.type === 'bar' && this.barGeometry) {
            const { padLeft, groupW, labels, datasets, barW, barPad } = this.barGeometry;
            const relX = this.mousePos.x - padLeft;
            const idx = Math.floor(relX / groupW);

            if (idx >= 0 && idx < labels.length) {
                const withinGroup = relX - idx * groupW - barPad;
                const dsi = Math.floor(withinGroup / barW);
                this.hoverIndex = idx;
                this.hoverDataset = Math.max(0, Math.min(datasets.length - 1, dsi));
                this.render();

                const ds = datasets[this.hoverDataset] || datasets[0];
                const val = (ds && ds.data && ds.data[idx] != null) ? ds.data[idx] : 0;
                const html = `
                    <div style="font-weight:600;margin-bottom:4px;color:${this.theme.accentPrimary}">${labels[idx]}</div>
                    <div style="display:flex;justify-content:space-between;gap:12px;">
                        <span style="color:${this.theme.textSecondary}">${ds.label || 'Value'}:</span>
                        <strong style="color:#ffffff">${Utils.formatCurrency(val)}</strong>
                    </div>
                `;
                this.showTooltip(html, this.mousePos.screenX, this.mousePos.screenY);
            } else {
                this.hideTooltip();
            }
        } else if ((this.type === 'doughnut' || this.type === 'pie') && this.doughnutGeometry && this.segments) {
            const { cx, cy, radius, innerRadius } = this.doughnutGeometry;
            const dx = this.mousePos.x - cx;
            const dy = this.mousePos.y - cy;
            const dist = Math.sqrt(dx * dx + dy * dy);

            if (dist >= innerRadius && dist <= radius + 6) {
                let angle = Math.atan2(dy, dx);
                if (angle < -Math.PI / 2) angle += Math.PI * 2;

                const segIdx = this.segments.findIndex(s => angle >= s.startAngle && angle < s.endAngle);
                if (segIdx >= 0) {
                    this.hoverIndex = segIdx;
                    this.render();
                    const s = this.segments[segIdx];
                    const html = `
                        <div style="font-weight:600;margin-bottom:2px;color:${s.color}">${s.label}</div>
                        <div style="font-size:13px;font-weight:700;">${Utils.formatCurrency(s.val)}</div>
                    `;
                    this.showTooltip(html, this.mousePos.screenX, this.mousePos.screenY);
                    return;
                }
            }
            this.hoverIndex = -1;
            this.hideTooltip();
            this.render();
        }
    }

    roundRect(x, y, w, h, r = 4, fill = true, stroke = false) {
        if (h <= 0) return;
        r = Math.min(r, w / 2, h / 2);
        this.ctx.beginPath();
        this.ctx.moveTo(x + r, y);
        this.ctx.lineTo(x + w - r, y);
        this.ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        this.ctx.lineTo(x + w, y + h);
        this.ctx.lineTo(x, y + h);
        this.ctx.lineTo(x, y + r);
        this.ctx.quadraticCurveTo(x, y, x + r, y);
        this.ctx.closePath();
        if (fill) this.ctx.fill();
        if (stroke) this.ctx.stroke();
    }
}
