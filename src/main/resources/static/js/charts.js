 // Charts initialization for reports page

 let familyChart = null;
 let pointsChart = null;
 let categoryCharts = [];

 function initCharts(weeklyStats) {
     if (!weeklyStats) return;

     // Family Time Distribution Chart
     const familyCtx = document.getElementById('familyChart')?.getContext('2d');
     if (familyCtx && weeklyStats.userTotalMinutes && Object.keys(weeklyStats.userTotalMinutes).length > 0) {
         if (familyChart) familyChart.destroy();

         familyChart = new Chart(familyCtx, {
             type: 'bar',
             data: {
                 labels: Object.keys(weeklyStats.userTotalMinutes),
                 datasets: [{
                     label: 'Minutes spent',
                     data: Object.values(weeklyStats.userTotalMinutes),
                     backgroundColor: ['#4CAF50', '#2196F3', '#FF9800', '#9C27B0', '#F44336'],
                     borderColor: '#fff',
                     borderWidth: 2
                 }]
             },
             options: {
                 responsive: true,
                 maintainAspectRatio: true,
                 plugins: {
                     legend: { position: 'top' },
                     title: { display: false }
                 },
                 scales: {
                     y: { beginAtZero: true, title: { display: true, text: 'Minutes' } },
                     x: { title: { display: true, text: 'Family Members' } }
                 }
             }
         });
     }

     // Points Distribution Chart
     const pointsCtx = document.getElementById('pointsChart')?.getContext('2d');
     if (pointsCtx && weeklyStats.userPoints && Object.keys(weeklyStats.userPoints).length > 0) {
         if (pointsChart) pointsChart.destroy();

         pointsChart = new Chart(pointsCtx, {
             type: 'pie',
             data: {
                 labels: Object.keys(weeklyStats.userPoints),
                 datasets: [{
                     data: Object.values(weeklyStats.userPoints),
                     backgroundColor: ['#FFD700', '#FFA500', '#FF6347', '#FF69B4', '#9370DB'],
                     borderColor: '#fff',
                     borderWidth: 2
                 }]
             },
             options: {
                 responsive: true,
                 maintainAspectRatio: true,
                 plugins: {
                     legend: { position: 'top' },
                     tooltip: { callbacks: { label: (ctx) => `${ctx.label}: ${ctx.raw} points` } }
                 }
             }
         });
     }

     // Category Charts per Member
     if (weeklyStats.userCategoryMinutes) {
         let chartIndex = 0;
         for (const [memberName, categories] of Object.entries(weeklyStats.userCategoryMinutes)) {
             if (categories && Object.keys(categories).length > 0) {
                 const canvasId = `categoryChart-${chartIndex}`;
                 const canvas = document.getElementById(canvasId);
                 if (canvas) {
                     const ctx = canvas.getContext('2d');
                     new Chart(ctx, {
                         type: 'doughnut',
                         data: {
                             labels: Object.keys(categories),
                             datasets: [{
                                 data: Object.values(categories),
                                 backgroundColor: ['#4CAF50', '#2196F3', '#FF9800', '#9C27B0', '#F44336', '#3F51B5', '#8BC34A'],
                                 borderColor: '#fff',
                                 borderWidth: 2
                             }]
                         },
                         options: {
                             responsive: true,
                             maintainAspectRatio: true,
                             plugins: {
                                 legend: { position: 'right', labels: { font: { size: 10 } } },
                                 title: { display: true, text: memberName }
                             }
                         }
                     });
                     chartIndex++;
                 }
             }
         }
     }
 }

 function initFamilyTimeChart(data) {
     const ctx = document.getElementById('familyTimeChart')?.getContext('2d');
     if (!ctx || !data) return;

     const labels = Object.keys(data);
     const values = Object.values(data);

     return new Chart(ctx, {
         type: 'bar',
         data: {
             labels: labels,
             datasets: [{
                 label: 'Hours',
                 data: values.map(v => Math.round(v / 60 * 10) / 10),
                 backgroundColor: 'rgba(102, 126, 234, 0.7)',
                 borderColor: '#667eea',
                 borderWidth: 2
             }]
         },
         options: {
             responsive: true,
             maintainAspectRatio: true,
             scales: { y: { beginAtZero: true, title: { display: true, text: 'Hours' } } }
         }
     });
 }