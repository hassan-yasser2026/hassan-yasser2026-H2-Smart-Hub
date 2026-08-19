import React, { useState, useRef, useEffect } from 'react';
import { 
  View, 
  TextInput, 
  Button, 
  Text, 
  FlatList, 
  StyleSheet, 
  KeyboardAvoidingView, 
  Platform, 
  ActivityIndicator, 
  Image, 
  Modal, 
  TouchableOpacity 
} from 'react-native';
import { askAI } from './apiService';

const img1 = require('./assets/ai_coming_soon-1.jpeg');
const img2 = require('./assets/ai_coming_soon-2.jpeg');
const img3 = require('./assets/ai_coming_soon-3.jpeg');

const comingSoonImages = [img1, img2, img3];

export default function ChatScreen() {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalImage, setModalImage] = useState(img1);
  const flatListRef = useRef();

  const handleSend = async () => {
    if (!input.trim()) return;
    
    const userText = input.trim();
    setLoading(true);

    const userMsg = { id: Date.now().toString(), role: 'user', content: userText };
    setMessages(prev => [...prev, userMsg]);
    setInput('');

    // Check for image or video generation keywords to show the Coming Soon artwork
    const lowerText = userText.toLowerCase();
    const isImageOrVideoRequest = 
      lowerText.includes('فيديو') || 
      lowerText.includes('video') || 
      lowerText.includes('صورة') || 
      lowerText.includes('صوره') || 
      lowerText.includes('image') || 
      lowerText.includes('توليد') || 
      lowerText.includes('رسم') || 
      lowerText.includes('صمم') || 
      lowerText.includes('art') || 
      lowerText.includes('فن');

    if (isImageOrVideoRequest) {
      // Pick a random image from the pool to keep the experience fresh and delightful
      const randomIndex = Math.floor(Math.random() * comingSoonImages.length);
      setModalImage(comingSoonImages[randomIndex]);
      setShowModal(true);
      
      // Also add an AI message response indicating coming soon status
      const aiMsg = { 
        id: (Date.now() + 1).toString(), 
        role: 'assistant', 
        content: "ميزة إنشاء وتوليد الصور والفيديوهات بالذكاء الاصطناعي (Veo 3 / Gemini) ستكون متوفرة قريباً جداً في التحديث القادم! 🚀" 
      };
      setMessages(prev => [...prev, aiMsg]);
      setLoading(false);
      setTimeout(() => flatListRef.current?.scrollToEnd(), 100);
      return;
    }

    try {
      const aiReply = await askAI(userText);
      const aiMsg = { id: (Date.now()+1).toString(), role: 'assistant', content: aiReply };
      setMessages(prev => [...prev, aiMsg]);
    } catch (e) {
      const errMsg = { id: (Date.now()+1).toString(), role: 'assistant', content: "كل الـ APIs وقعت 😭 جرب تاني" };
      setMessages(prev => [...prev, errMsg]);
    }
    setLoading(false);
    setTimeout(() => flatListRef.current?.scrollToEnd(), 100);
  };

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === "ios"? "padding" : "height"}>
      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={item.role === 'user'? styles.userBubble : styles.aiBubble}>
            <Text style={item.role === 'user'? styles.userText : styles.aiText}>{item.content}</Text>
          </View>
        )}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd()}
      />
      {loading && <ActivityIndicator size="small" color="#007AFF" />}
      <View style={styles.inputRow}>
        <TextInput
          style={styles.input}
          value={input}
          onChangeText={setInput}
          placeholder="اسألني اي حاجة..."
          multiline
        />
        <Button title="ارسال" onPress={handleSend} disabled={loading} />
      </View>

      {/* Coming Soon Modal */}
      <Modal
        transparent={true}
        visible={showModal}
        animationType="fade"
        onRequestClose={() => setShowModal(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Image source={modalImage} style={styles.modalImage} resizeMode="contain" />
            <Text style={styles.modalText}>قريباً 🚀</Text>
            <Text style={styles.modalSubText}>ميزة إنشاء وتوليد المحتوى بالذكاء الاصطناعي</Text>
            <TouchableOpacity onPress={() => setShowModal(false)} style={styles.closeBtn}>
              <Text style={{ color: 'white', fontWeight: 'bold' }}>تمام</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F2F2F2', padding: 10 },
  inputRow: { flexDirection: 'row', alignItems: 'flex-end', paddingTop: 10, backgroundColor: '#fff' },
  input: { flex: 1, borderWidth: 1, borderColor: '#ddd', padding: 10, marginRight: 8, borderRadius: 20, maxHeight: 100, backgroundColor: '#fff', textAlign: 'right' },
  userBubble: { backgroundColor: '#007AFF', padding: 12, marginVertical: 4, borderRadius: 18, alignSelf: 'flex-end', maxWidth: '85%' },
  aiBubble: { backgroundColor: '#fff', padding: 12, marginVertical: 4, borderRadius: 18, alignSelf: 'flex-start', maxWidth: '85%', borderWidth: 1, borderColor: '#eee' },
  userText: { color: '#fff', fontSize: 16, textAlign: 'right' },
  aiText: { color: '#000', fontSize: 16, textAlign: 'right' },
  
  // Modal styles
  modalBackground: { flex: 1, backgroundColor: 'rgba(0,0,0,0.8)', justifyContent: 'center', alignItems: 'center' },
  modalContainer: { width: '85%', backgroundColor: '#222', borderRadius: 20, padding: 20, alignItems: 'center' },
  modalImage: { width: 220, height: 220, marginBottom: 15, borderRadius: 12 },
  modalText: { fontSize: 24, fontWeight: 'bold', color: 'white', marginBottom: 5, textAlign: 'center' },
  modalSubText: { fontSize: 14, color: '#aaa', marginBottom: 20, textAlign: 'center' },
  closeBtn: { backgroundColor: '#007AFF', paddingVertical: 10, paddingHorizontal: 30, borderRadius: 10 },
});