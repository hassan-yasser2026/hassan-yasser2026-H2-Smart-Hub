import React from 'react';
import { Button, Alert } from 'react-native';

const comingSoonImage = require('./assets/ai_coming_soon.jpg');

export default function ComingSoonButton() {
  const handleAICreate = () => {
      Alert.alert(
            "قريباً 🚀",
                  "ميزة إنشاء الفيديو بالذكاء الاصطناعي قريباً",
                        [{ text: "تمام", style: "cancel" }]
                            );
                              }

                                return (
                                    <Button 
                                          title="إنشاء فيديو AI" 
                                                onPress={handleAICreate} 
                                                      color="#6366F1"
                                                          />
                                                            );
                                                            }