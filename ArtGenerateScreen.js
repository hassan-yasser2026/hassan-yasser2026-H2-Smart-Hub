import React, { useState } from 'react';
import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  ScrollView, 
  StyleSheet, 
  Image, 
  Alert, 
  SafeAreaView, 
  StatusBar 
} from 'react-native';

const comingSoonImage = require('./assets/ai_coming_soon-1.jpeg');

export default function ArtGenerateScreen() {
  const [activeTab, setActiveTab] = useState('video'); // 'image' | 'video' | 'animate'
  const [inputText, setInputText] = useState('');
  const [selectedRatio, setSelectedRatio] = useState('square'); // '916' | '169' | 'square'

  const handleGeneratePress = () => {
    Alert.alert(
      "قريباً 🚀",
      "ميزة إنشاء وتوليد الفيديو بالذكاء الاصطناعي (Veo 3) ستكون متاحة قريباً في التحديث القادم!",
      [{ text: "حسناً", style: "default" }]
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor="#111" />
      
      {/* 1. HEADER */}
      <View style={styles.header}>
        {/* Settings Button on the left */}
        <TouchableOpacity style={styles.headerIconBtn} onPress={() => Alert.alert("الإعدادات", "قريباً ميزات الإعدادات المتقدمة")}>
          <Text style={styles.headerIconText}>⚙️</Text>
        </TouchableOpacity>

        {/* Title and H2 Badge in the Center */}
        <View style={styles.headerTitleContainer}>
          <Text style={styles.headerTitle}>توليد الفن</Text>
          <View style={styles.badge}>
            <Text style={styles.badgeText}>H2</Text>
          </View>
        </View>

        {/* Back Arrow on the right (RTL Layout) */}
        <TouchableOpacity style={styles.headerIconBtn}>
          <Text style={styles.headerIconText}>➡️</Text>
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContainer} showsVerticalScrollIndicator={false}>
        
        {/* 2. TABS */}
        <View style={styles.tabsContainer}>
          <TouchableOpacity 
            onPress={() => setActiveTab('animate')} 
            style={[styles.tabButton, activeTab === 'animate' && styles.activeTabButton]}
          >
            <Text style={[styles.tabButtonText, activeTab === 'animate' && styles.activeTabButtonText]}>
              تحريك الصور
            </Text>
          </TouchableOpacity>

          <TouchableOpacity 
            onPress={() => setActiveTab('video')} 
            style={[styles.tabButton, activeTab === 'video' && styles.activeTabButton]}
          >
            <Text style={[styles.tabButtonText, activeTab === 'video' && styles.activeTabButtonText]}>
              فيديو بالذكاء (Veo)
            </Text>
          </TouchableOpacity>

          <TouchableOpacity 
            onPress={() => setActiveTab('image')} 
            style={[styles.tabButton, activeTab === 'image' && styles.activeTabButton]}
          >
            <Text style={[styles.tabButtonText, activeTab === 'image' && styles.activeTabButtonText]}>
              توليد الصور
            </Text>
          </TouchableOpacity>
        </View>

        {/* 3. BANNER */}
        <View style={styles.bannerContainer}>
          <Image source={comingSoonImage} style={styles.bannerImage} resizeMode="cover" />
          <View style={styles.bannerOverlay}>
            <Text style={styles.bannerText}>ميزة توليد الصور والفيديو بالذكاء الاصطناعي (قريباً) 🚀</Text>
          </View>
        </View>

        {/* 4. INPUT BOX */}
        <View style={styles.inputSection}>
          <Text style={styles.sectionLabel}>وصف المشهد أو السيناريو المطلوب:</Text>
          <TextInput
            style={styles.textInput}
            multiline={true}
            numberOfLines={4}
            value={inputText}
            onChangeText={setInputText}
            placeholder="اكتب سيناريو للفيديو (Veo 3): كاميرا تتحرك في غابة فضائية سحرية مليئة بالنجوم الراقصة..."
            placeholderTextColor="#666"
          />
        </View>

        {/* 5. ALERT BOX */}
        <View style={styles.alertBox}>
          <View style={styles.alertTextContainer}>
            <Text style={styles.alertTitle}>نظام الحماية والأمان الصارم:</Text>
            <Text style={styles.alertDescription}>
              يمنع توليد وتعديل أي صور أو فيديوهات غير أخلاقية أو عارية أو مخالفة للقانون والدين.
            </Text>
          </View>
          <Text style={styles.alertIcon}>🛡️</Text>
        </View>

        {/* 6. ASPECT RATIO */}
        <View style={styles.ratioSection}>
          <Text style={styles.sectionLabel}>نسبة العرض والارتفاع (Aspect Ratio):</Text>
          <View style={styles.ratioButtonsContainer}>
            <TouchableOpacity 
              onPress={() => setSelectedRatio('916')}
              style={[styles.ratioButton, selectedRatio === '916' && styles.ratioButtonActive]}
            >
              <Text style={[styles.ratioButtonText, selectedRatio === '916' && styles.ratioButtonTextActive]}>
                9:16 رأسي
              </Text>
            </TouchableOpacity>

            <TouchableOpacity 
              onPress={() => setSelectedRatio('169')}
              style={[styles.ratioButton, selectedRatio === '169' && styles.ratioButtonActive]}
            >
              <Text style={[styles.ratioButtonText, selectedRatio === '169' && styles.ratioButtonTextActive]}>
                16:9 أفقي
              </Text>
            </TouchableOpacity>

            <TouchableOpacity 
              onPress={() => setSelectedRatio('square')}
              style={[styles.ratioButton, selectedRatio === 'square' && styles.ratioButtonSelectedDefault]}
            >
              <Text style={[styles.ratioButtonText, selectedRatio === 'square' && styles.ratioButtonTextActive]}>
                مربع
              </Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 7. BUTTON */}
        <TouchableOpacity style={styles.generateButton} onPress={handleGeneratePress}>
          <Text style={styles.generateButtonText}>توليد فيديو بالذكاء (Veo 3)</Text>
        </TouchableOpacity>

        {/* 8. RESULT CARD */}
        <View style={styles.resultSection}>
          <Text style={styles.sectionLabel}>الفيديو المولد بنجاح (Veo 3.1):</Text>
          <View style={styles.resultCard}>
            <Image source={comingSoonImage} style={styles.resultImage} resizeMode="cover" />
            <View style={styles.resultOverlay}>
              <View style={styles.playIconContainer}>
                <Text style={styles.playIcon}>▶️</Text>
              </View>
              <Text style={styles.resultSubtitle}>تشغيل الفيديو: 1:1</Text>
            </View>
          </View>
        </View>

        {/* Bottom spacing for bottom navigation */}
        <View style={styles.bottomSpacing} />
      </ScrollView>

      {/* 9. BOTTOM NAV */}
      <View style={styles.bottomNav}>
        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>📖</Text>
          <Text style={styles.navText}>مصحح التلاوة</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>📅</Text>
          <Text style={styles.navText}>المنظم اليومي</Text>
        </TouchableOpacity>

        <TouchableOpacity style={[styles.navItem, styles.navItemActive]}>
          <Text style={[styles.navIcon, styles.navIconActive]}>🎨</Text>
          <Text style={[styles.navText, styles.navTextActive]}>توليد الفن</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>👤</Text>
          <Text style={styles.navText}>شخصيات AI</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>⚡</Text>
          <Text style={styles.navText}>الإنتاجية</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>🐱</Text>
          <Text style={styles.navText}>Smart Cat</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navIcon}>🏠</Text>
          <Text style={styles.navText}>الرئيسية</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#111',
  },
  scrollContainer: {
    padding: 16,
    paddingBottom: 100, // Leave space for Bottom Nav
  },
  bottomSpacing: {
    height: 20,
  },
  
  // 1. HEADER STYLES
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#222',
    backgroundColor: '#111',
  },
  headerIconBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#1e1e1e',
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerIconText: {
    fontSize: 18,
    color: '#fff',
  },
  headerTitleContainer: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    marginLeft: 8,
  },
  badge: {
    backgroundColor: '#00D1B2',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 6,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '900',
    color: '#111',
  },

  // 2. TABS STYLES
  tabsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#1a1a1a',
    borderRadius: 12,
    padding: 4,
    marginBottom: 20,
  },
  tabButton: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    borderRadius: 8,
  },
  activeTabButton: {
    backgroundColor: '#111',
    borderBottomWidth: 2,
    borderBottomColor: '#00D1B2',
  },
  tabButtonText: {
    fontSize: 11,
    color: '#888',
    fontWeight: '600',
  },
  activeTabButtonText: {
    color: '#00D1B2',
    fontWeight: 'bold',
  },

  // 3. BANNER STYLES
  bannerContainer: {
    height: 150,
    borderRadius: 12,
    overflow: 'hidden',
    marginBottom: 20,
    position: 'relative',
    borderWidth: 1,
    borderColor: '#333',
  },
  bannerImage: {
    width: '100%',
    height: '100%',
  },
  bannerOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.6)',
    justifyContent: 'flex-end',
    padding: 14,
  },
  bannerText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: 'bold',
    textAlign: 'right',
    lineHeight: 18,
  },

  // 4. INPUT BOX STYLES
  inputSection: {
    marginBottom: 20,
  },
  sectionLabel: {
    color: '#aaa',
    fontSize: 12,
    fontWeight: 'bold',
    marginBottom: 8,
    textAlign: 'right',
  },
  textInput: {
    backgroundColor: '#1a1a1a',
    borderColor: '#00D1B2',
    borderWidth: 1.5,
    borderRadius: 12,
    padding: 12,
    color: '#fff',
    fontSize: 13,
    textAlign: 'right',
    minHeight: 100,
    textAlignVertical: 'top',
  },

  // 5. ALERT BOX STYLES
  alertBox: {
    flexDirection: 'row',
    backgroundColor: '#FFD1D1',
    borderRadius: 12,
    padding: 12,
    marginBottom: 20,
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  alertTextContainer: {
    flex: 1,
    marginRight: 10,
  },
  alertTitle: {
    color: '#8B0000',
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'right',
    marginBottom: 2,
  },
  alertDescription: {
    color: '#A52A2A',
    fontSize: 10.5,
    textAlign: 'right',
    lineHeight: 15,
  },
  alertIcon: {
    fontSize: 24,
  },

  // 6. ASPECT RATIO STYLES
  ratioSection: {
    marginBottom: 24,
  },
  ratioButtonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  ratioButton: {
    flex: 1,
    backgroundColor: '#1a1a1a',
    paddingVertical: 12,
    borderRadius: 10,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#333',
  },
  ratioButtonActive: {
    borderColor: '#00D1B2',
    backgroundColor: '#222',
  },
  ratioButtonSelectedDefault: {
    backgroundColor: '#2e2e2e',
    borderColor: '#555',
  },
  ratioButtonText: {
    color: '#888',
    fontSize: 11,
    fontWeight: '600',
  },
  ratioButtonTextActive: {
    color: '#fff',
    fontWeight: 'bold',
  },

  // 7. BUTTON STYLES
  generateButton: {
    backgroundColor: '#333',
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 24,
    borderWidth: 1,
    borderColor: '#444',
  },
  generateButtonText: {
    color: '#aaa',
    fontSize: 14,
    fontWeight: 'bold',
  },

  // 8. RESULT CARD STYLES
  resultSection: {
    marginBottom: 20,
  },
  resultCard: {
    height: 180,
    borderRadius: 12,
    overflow: 'hidden',
    position: 'relative',
    borderWidth: 1,
    borderColor: '#333',
  },
  resultImage: {
    width: '100%',
    height: '100%',
  },
  resultOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  playIconContainer: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 2,
    borderColor: '#00D1B2',
  },
  playIcon: {
    fontSize: 24,
    color: '#fff',
    marginLeft: 4, // Center look for play icon
  },
  resultSubtitle: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },

  // 9. BOTTOM NAV STYLES
  bottomNav: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: 70,
    backgroundColor: '#161616',
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: '#222',
    paddingBottom: 4,
  },
  navItem: {
    alignItems: 'center',
    flex: 1,
  },
  navItemActive: {
    transform: [{ scale: 1.05 }],
  },
  navIcon: {
    fontSize: 18,
    color: '#888',
    marginBottom: 2,
  },
  navIconActive: {
    color: '#00D1B2',
  },
  navText: {
    fontSize: 7.5,
    color: '#888',
    fontWeight: '500',
  },
  navTextActive: {
    color: '#00D1B2',
    fontWeight: 'bold',
  },
});
